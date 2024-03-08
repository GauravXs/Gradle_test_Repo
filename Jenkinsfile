/* groovylint-disable DuplicateStringLiteral */
def new_war_file = ''
def javaVer = ['Java8', 'Java11', 'Java17']

    pipeline {
        agent any

        tools {
            //maven 'jenkins-maven'
            gradle 'jenkins-gradle'
            //jdk 'Java11'//, version: '11.0.22'
            jdk 'Java17'//, version: '17.0.10'
            //jdk 'Java8'//, version: '8u392-ga-1'
            //jdk 'Java21'//, version: '21.0.2'
        }

        environment {
            backup_dir = '/backup'
            TC_webapp_dir = '/opt/tomcat/webapps'
            TC_war_file_path = "\${TC_webapp_dir}/*.war"
            //mavenHome = tool 'jenkins-maven'
            github_repo_url = 'https://github.com/gauravxs/gradle-simple.git'
        }

        stages {
            stage('Pre-Condition Check') {
                steps {
                    script {
                        // Check GitHub repository availability
                        sh """
                            echo "Checking GitHub repository availability..."
                            git ls-remote --exit-code \${github_repo_url} || (echo "GitHub repository is not reachable, exiting." && exit 1)
                            echo "GitHub repository is reachable. Proceeding with the pipeline."
                        """
                    }
                }
            }

            stage('Clean Workspace') {
                steps {
                    script {
                        cleanWs()
                    //sh "rm -rf \${JENKINS_HOME}/workspace/\${JOB_NAME}/*"
                    }
                }
            }

            stage('Git Pull') {
                steps {
                    script {
                        sh 'echo "Pulling from Github Repo"'
                        checkout([$class: 'GitSCM', branches: [[name: '*/master']],
                        doGenerateSubmoduleConfigurations: false, extensions: [],
                        submoduleCfg: [],
                        userRemoteConfigs: [[url: "$github_repo_url"]]])
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        sh "ls -lah ${JENKINS_HOME}/workspace/${JOB_NAME}/"
                        //sh "ls -lah ${JENKINS_HOME}/workspace/${JOB_NAME}/build/libs"
                        echo "Building Project Artifact using Gradle"
                        sh "./gradlew clean build"
                        sh "ls -lah ${JENKINS_HOME}/workspace/${JOB_NAME}/build/libs"
                    }
                }
            }
        }
    }
