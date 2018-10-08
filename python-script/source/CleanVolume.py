import sys
import time
import os
import sys
import boto3

session = boto3.session.Session(profile_name='doubledigit') #NOT Required when running in ec2
client = session.client('ec2')

print('Finding Volumes...')
response = client.describe_volumes(
    Filters=[
        {
            'Name':'status',
            'Values':[
                'available'
            ]
        }
    ]
)

if len(response['Volumes']) == 0:
    print('Found None..')
    sys.exit()

print('Foune' + str(len(response['Volumes'])))

#MAKE A  LIST OT HOLD SNAPSHOTS ID
snapShotsIds = []

#iterate through volumes and snapshot them all
for volume in response['Volumes']:
    snapShot = client.create_snapshot(
        VolumeId = volume['VolumeId']
    )

    snapShotsIds.append(snapShot['SnapshotId'])

print('Waiting for snapshots...')
waiter = client.get_waiter('snapshot_completed')
waiter.wait(
    SnapshotIds=snapShotsIds
)


print('Deleting volumes....')

for volume in response['Volumes']:
    client.delete_volume(VolumeId=volume['VolumeId'])