import boto3
import sys
import os
import time
import datetime

#profile name is only for local run
session = boto3.session.Session(profile_name='dev')
client = session.client('rds')

#create second client in other region to create snanp_shot_copy
clientWest = session.client('rds',region_name='us-west-2')

#Find RDS DB instances
print('Finding DB Instances...')
response = client.describe_db_instaces()

dbInstances = response['DBInstances']

#for each instance create snap_shot
for dbInstance in dbInstances:
    print('Creating Snapshot for DB Instance ' + dbInstance['DBName'])

    response = client.create_db_snapshot(
        DBSnapshotIdentifier=dbInstance['DBName'] + '-' + datetime.datetime.today().strftime('%Y-%m-%d'),
        DBInstanceIdentifier=dbInstance['DBInstanceIdentifier']
    )
    dbSnapshot = response['DBSnapshot']

    #wait for snapshot to complete
    waiter = client.get_waiter('db_snapshot_complete')
    waiter.wait(DBSnapshotIdentifier=dbSnapshot['DBSnapshotIdentifier'])

    #copy to another region
    print('Copying to another region...')
    clientWest.copy_db_snapshot(
        SourceDBSnapshotIdentifier='arn:aws:rds:us-east-2:<ACC_NO>:snapshot:'+dbSnapshot['DBSnapshotIdentifier'],
        TargetSnapshotIdentifier=dbSnapshot['DBSnapshotIdentifier'],
        CopyTags=True,
        SourceRegion='us-east-2'
    )

print('Done!')

