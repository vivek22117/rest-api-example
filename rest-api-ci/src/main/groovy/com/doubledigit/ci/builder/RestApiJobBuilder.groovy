package com.doubledigit.ci.builder

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

/**
 * Created by Vivek Kumar Mishra on 04/10/2018.
 */
class RestApiJobBuilder {
    DslFactory dslFactory;
    String shellCommand;
    String jobName;
    String taskName;
    String contactEmail;
    String applicationRepoUrl;
    String environment;

    Job build() {
        dslFactory.freeStyleJob(jobName){ Job job ->
            logRotator{
                numToKeep(10)
            }

            deliveryPipelineConfiguration(environment, taskName)

            defaultGit(job, applicationRepoUrl,'${appVersion}')

            parameters {
                stringParam('appVersion', 'master')
            }
            if(environment == 'production'){
                triggers {
                    scm('H H * * *')
                }
            }
            wrappers {
                timestamps()
                environmentVariables(environment: environment)
                buildName('${GIT_VERSION, length=8}')
            }
            label(environment)
            steps {
                shell(shellCommand)
            }

            publishers {
                mailer(contactEmail, false, true)
                if(environment == 'production'){
                    //do this
                }
                downstreamParameterized {
                    trigger('my-next-job-'+environment) {triggerWithNoParameters()}
                }
            }
        }
    }
}
