/* groovylint-disable DuplicateStringLiteral */
def new_war_file = ''
def javaVer = ['Java8', 'Java11', 'Java17']

    pipeline {
        agent any

        tools {
            //maven 'jenkins-maven'
            //gradle 'jenkins-gradle'
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
            github_repo_url = 'https://github.com/GauravXs/Gradle_test_Repo.git'
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
                        checkout([$class: 'GitSCM', branches: [[name: '*/main']],
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

            stage('Stopping Tomcat') {
                steps {
                    script {
                        sh 'echo "Checking Tomcat Status..."'
                        sh '''if sudo systemctl is-active --quiet tomcat; then
                                echo "Tomcat is running. Stopping..."
                                sudo systemctl stop tomcat
                            else
                                echo "Tomcat is not running."
                            fi'''
                    }
                }
            }

            stage('Backup') {
                steps {
                    script {
                        new_war_file = sh(script: 'basename ${JENKINS_HOME}/workspace/\${JOB_NAME}/target/*.war', returnStdout: true).trim()
                        echo "New WAR file: ${new_war_file}"
                        env.NEW_WAR_FILE = new_war_file
                        echo "Value for new_war_file -> $new_war_file"

                        sh """
                    echo "Content of target dir -> "
                    ls -lah \${JENKINS_HOME}/workspace/\${JOB_NAME}/target/

                    if ls ${TC_war_file_path} 1> /dev/null 2>&1; then

                        if ls $TC_webapp_dir/$new_war_file 1> /dev/null 2>&1; then

                            echo "Older version of \${new_war_file} already exists in \${TC_webapp_dir}"
                            echo "$new_war_file"

                            echo "$backup_dir/\$(basename -s .war $TC_webapp_dir/$new_war_file)_WAR_BK_\$(date +%Y-%m-%d_%H-%M-%S).war"

                            echo "Backing up $new_war_file to $backup_dir/\$(basename -s .war $TC_webapp_dir/$new_war_file)_WAR_BK_\$(date +%Y-%m-%d_%H-%M-%S).war"
                            cp -r "$TC_webapp_dir/$new_war_file" "$backup_dir/\$(basename -s .war $TC_webapp_dir/$new_war_file)_WAR_BK_\$(date +%Y-%m-%d_%H-%M-%S).war"

                            echo "Backing up $new_war_file folder to $backup_dir directory as an archieve"
                            zip -vr "$backup_dir/\$(basename -s .war $TC_webapp_dir/$new_war_file)_ZIP_BK_\$(date +%Y-%m-%d_%H-%M-%S).zip" -j "$TC_webapp_dir/\$(basename -s .war $new_war_file)"/*

                            echo "Removing existing WAR file..."
                            rm "$TC_webapp_dir/$new_war_file"

                            sudo chmod -R 770 \${TC_webapp_dir}
                            echo "Removing existing Application Folder..."
                            rm -rf "$TC_webapp_dir/\$(basename -s .war $new_war_file)"
                        else
                            echo "No new WAR file Generated. Skipping Backup"
                        fi
                    else
                        echo "No existing Tomcat WAR file found in \$TC_webapp_dir"
                    fi
                """
                    }
                }
            }

            stage('Deployment') {
                steps {
                    script {
                        sh '''
                            echo "Copying new WAR file to Tomcat..."
                            cp \${JENKINS_HOME}/workspace/\${JOB_NAME}/target/*.war \${TC_webapp_dir}
                            sleep 5
                            sudo chown -R tomcat:tomcat \${TC_webapp_dir}
                        '''
                    }
                }
            }

            stage('Start Tomcat') {
                steps {
                    script {
                        sh '''
                        echo "Starting Tomcat Server..."
                        sudo systemctl daemon-reload && sudo systemctl start tomcat.service
                        echo "Deployment completed successfully."
                            '''
                    }
                }
            }
        }

        post {
            always {
                script {
                    if (currentBuild.currentResult == 'FAILURE') {
                        //sh 'echo "Build result has changed to FAILURE"'
                        emailext subject: "${FAIL_SUBJECT}",
                            body: "${FAIL_CONTENT}",
                            attachLog: true,
                            recipientProviders: [
                                [$class: 'CulpritsRecipientProvider'],
                                [$class: 'DevelopersRecipientProvider'],
                                [$class: 'RequesterRecipientProvider']
                            ],
                            replyTo: "${DEFAULT_REPLYTO}",
                            to: "${RECIPIENTS_NAME}"
                    } else if (currentBuild.currentResult == 'UNSTABLE') {
                        //sh 'echo "Build result has changed to UNSTABLE"'
                        emailext subject: "${UNSTABLE_SUBJECT}",
                            body: "${UNSTABLE_CONTENT}",
                            attachLog: true,
                            recipientProviders: [
                                [$class: 'CulpritsRecipientProvider'],
                                [$class: 'DevelopersRecipientProvider'],
                                [$class: 'RequesterRecipientProvider']
                            ],
                            replyTo: "${DEFAULT_REPLYTO}",
                            to: "${RECIPIENTS_NAME}"
                    } else if (currentBuild.currentResult == 'SUCCESS') {
                        sh 'echo "Build SUCCESS"'
                        /*
                        emailext subject: "${PASS_SUBJECT}",
                            body: "${PASS_CONTENT}",
                            attachLog: true,
                            recipientProviders: [
                                [$class: 'CulpritsRecipientProvider'],
                                [$class: 'DevelopersRecipientProvider'],
                                [$class: 'RequesterRecipientProvider']
                            ],
                            replyTo: "${DEFAULT_REPLYTO}",
                            to: "${RECIPIENTS_NAME}"
                    //////////////////*/
                    } else {
                        //sh 'echo "Build result has changed to UNKNOWN"'
                        emailext subject: "Unknown Build ${BUILD_NUMBER}",
                            body: "Unknown Build ${BUILD_NUMBER} ",
                            attachLog: true,
                            recipientProviders: [
                                [$class: 'CulpritsRecipientProvider'],
                                [$class: 'DevelopersRecipientProvider'],
                                [$class: 'RequesterRecipientProvider']
                            ],
                            replyTo: "${DEFAULT_REPLYTO}",
                            to: "${RECIPIENTS_NAME}"
                    }
                }
            }
        }
    }
