package com.doubledigit.ci.scirpts

import com.doubledigit.ci.builder.RestApiJobBuilder
import javaposse.jobdsl.dsl.JobParent


def factory = this as JobParent
def taskName = "Spark Job Builder"
def jobName = "spark-job-builder"
def devlJobName = jobName + '-development'
def cerJobName = jobName + '-testing'
def conactEmail = "vivekmishra1487@gmail.com"
def applicationURL = ""

def shellCommand = [
        '#!/bin/bash -el',
        'export environment=$environment',
        'jarPath = $WORKSPACE/sparkapp/target/*.jar',  // use this variable to move jar from one place to another
        'cd sparkapp',
        'mvn builder:create-metadata install -s -DskipTests -U',
        'mvn versions:set -DgeneratedBackupPoms=false -DnewVersion=${GIT_COMMIT} -s -DskipTests',
        'mvn clean install -U'
].join('\n')

def verificationTest = [
        '#!/bin/bash -el',
        'export environment=$environment',
        'if ["$environment" != "prod"]',
        'then' ,
        'cd spark-verificationTest',
        'mvn versions:set -DgeneratedBackupPoms=false -DnewVersion=${GIT_COMMIT} -s -DskipTests',
        'mvn test -s -DEnvironment=${environment}'
].join('\n')


new RestApiJobBuilder(
        dslFactory: factory,
        shellCommand: shellCommand,
        jobName: devlJobName,
        taskName: taskName,
        contactEmail: conactEmail,
        applicationRepoUrl: applicationURL,
        environment: 'development'

).build()