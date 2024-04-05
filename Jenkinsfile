/* groovylint-disable DuplicateStringLiteral */
def new_war_file = ''
def javaVer = ['Java8', 'Java11', 'Java17']

    pipeline {
        agent any

        tools {
            //maven 'jenkins-maven'
            gradle 'jenkins-gradle'
            jdk 'Java11'//, version: '11.0.22'
            //jdk 'Java17'//, version: '17.0.10'
            //jdk 'Java8'//, version: '8u392-ga-1'
            //jdk 'Java21'//, version: '21.0.2'
        }

        environment {
            backup_dir = '/backup'
            TC_webapp_dir = '/opt/tomcat/webapps'
            TC_war_file_path = "\${TC_webapp_dir}/*.war"
            github_repo_url = 'https://github.com/GauravXs/Gradle_test_Repo.git'

            //Variables for Sonarqube Server
            sonarqube_ip = '10.1.1.46' //Only IPv4 and no need to add http:// or https://
            sonarqube_port = '9000'
            sonar_login_token = 'sqp_ed0dadd968e141a3aa87d86d039b01506f843000'
            sonar_project_key = 'GradlePipeline'

            //Variables for Nexus Server
            NEXUS_VERSION = 'nexus3'
            NEXUS_PROTOCOL = 'http'
            NEXUS_IP = '10.1.1.46' //Only IPv4 and no need to add http:// or https://
            NEXUS_PORT = '8081'
            NEXUS_URL = '10.1.1.46:8081'
            NEXUS_REPOSITORY = 'in28Minutes-first-webapp'
            NEXUS_CREDENTIAL_ID = 'nexus_cred'
            ARTIFACT_VERSION = '\${BUILD_NUMBER}'

            //E-MAIL Variables
            REPLYTO_NAME = 'gourav.singh@mobicule.com'
            RECIPIENTS_NAME = 'gourav.singh@mobicule.com'

            PASS_SUBJECT = '\${DEFAULT_SUBJECT}'
            PASS_CONTENT = 'Jenkins Pipeline Build \${BUILD_NUMBER} Passed Successfully \n \${DEFAULT_CONTENT}'
            UNSTABLE_SUBJECT = '\${DEFAULT_SUBJECT}'
            UNSTABLE_CONTENT = 'Jenkins Pipeline Build ${BUILD_NUMBER} is UNSTABLE \${DEFAULT_CONTENT}'
            FAIL_SUBJECT = '\${DEFAULT_SUBJECT}'
            FAIL_CONTENT = 'Jenkins Pipeline Build \${BUILD_NUMBER} FAILED... Attention Required! \${DEFAULT_CONTENT}'

            //DEFAULT_SUBJECT = 'Default Subject'
            //DEFAULT_CONTENT = 'Default Content'
            DEFAULT_REPLYTO = 'Gourav.singh@mobicule.com'

            useSonar = 'true'
            useNexus = 'true'
            //javaDeploy = 'Java17'
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

                        if (useSonar == 'true') {
                        // Check SonarQube Server availability
                        sh """
                            echo "Checking SonarQube availability..."
                            nc -zv -w5 \${sonarqube_ip} \${sonarqube_port} || (echo "SonarQube is not reachable on http://\${sonarqube_ip}:\${sonarqube_port}, exiting." && exit 1)
                            echo "SonarQube Server is reachable. Continuing with Test"
                        """
                        }
                        if (useNexus == 'true') {
                        // Check Nexus Repo Server availability
                        sh """
                            echo "Checking SonarQube availability..."
                            nc -zv -w5 \${NEXUS_IP} \${NEXUS_PORT} || (echo "Nexus Repo Server is not reachable on http://\${NEXUS_IP}:\${NEXUS_PORT}, exiting." && exit 1)
                            echo "Nexus Repo Server is reachable. Proceeding with the pipeline."
                        """
                    }
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

            stage('SonarQube Analysis') {
                when {
                    expression { useSonar == 'true' }
                }
                steps {
                    script {
                        echo "Running SonarQube analysis..."
                        sh "gradle sonarqube -Dsonar.projectKey=\$sonar_project_key -Dsonar.host.url=http://\${sonarqube_ip}:\${sonarqube_port} -Dsonar.login=\${sonar_login_token}"
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

            stage('Publish to Nexus') {
                when {
                    expression { useNexus == 'true' }
                }
                steps {
                    script {
                        pom = readMavenPom file: 'pom.xml'
                        filesByGlob = findFiles(glob: "target/*.${pom.packaging}")
                        echo "\${filesByGlob[0].name} \${filesByGlob[0].path} \${filesByGlob[0].directory} \${filesByGlob[0].length} \${filesByGlob[0].lastModified}"
                        if (filesByGlob) {
                            artifactPath = filesByGlob[0].path
                            artifactExists = fileExists artifactPath
                            if (artifactExists) {
                                echo "*** File: \${artifactPath}, group: \${pom.groupId}, packaging: \${pom.packaging}, version \${pom.version}"

                                nexusArtifactUploader(
                                nexusVersion: NEXUS_VERSION,
                                protocol: NEXUS_PROTOCOL,
                                nexusUrl: NEXUS_URL,
                                groupId: pom.groupId,
                                version: ARTIFACT_VERSION,
                                repository: NEXUS_REPOSITORY,
                                credentialsId: NEXUS_CREDENTIAL_ID,
                                artifacts: [
                                    [artifactId: pom.artifactId,
                                    classifier: '',
                                    file: artifactPath,
                                    type: pom.packaging]
                                ]
                            )
                            } else {
                                error "*** File: \${artifactPath}, could not be found"
                            }
                        } else {
                            error '*** No files found matching the specified pattern.'
                        }
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
                        new_war_file = sh(script: 'basename ${JENKINS_HOME}/workspace/\${JOB_NAME}/build/libs/*.war', returnStdout: true).trim()
                        echo "New WAR file: ${new_war_file}"
                        env.NEW_WAR_FILE = new_war_file
                        echo "Value for new_war_file -> $new_war_file"

                        sh """
                    echo "Content of target dir -> "
                    ls -lah \${JENKINS_HOME}/workspace/\${JOB_NAME}/build/libs/

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
                            cp \${JENKINS_HOME}/workspace/\${JOB_NAME}/build/libs/*.war \${TC_webapp_dir}
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
                        sh 'echo "Build FAILURE"'
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
                        sh 'echo "Build UNSTABLE"'
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
                        sh 'echo "Build UNKNOWN"'
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
