pipeline {
    options {
        timeout(time: 1, unit: 'HOURS')
        gitLabConnection('gitlab.nextfaze.com')
    }
    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    agent { label 'android' }

    environment {
        MAVEN = credentials('jenkins-maven')
    }

    stages {
        stage('Assemble') {
            steps {
                gitlabCommitStatus(name: 'build') {
                    sh """./gradlew clean assemble \
                        -PnextfazeArtifactoryUser="$MAVEN_USR" \
                        -PnextfazeArtifactoryPassword="$MAVEN_PSW"
                    """
                }
            }
        }

        stage('Lint') {
            steps {
                gitlabCommitStatus(name: 'lint') {
                    sh """./gradlew lint \
                        -PnextfazeArtifactoryUser="$MAVEN_USR" \
                        -PnextfazeArtifactoryPassword="$MAVEN_PSW"
                    """
                }
            }
            post {
                always {
                    androidLint canRunOnFailed: true, defaultEncoding: '', healthy: '0', pattern: '**/lint-results*.xml', unHealthy: '10'
                }
            }
        }

        stage('Test') {
            steps {
                gitlabCommitStatus(name: "test") {
                    sh """./gradlew --continue test \
                        -PnextfazeArtifactoryUser="$MAVEN_USR" \
                        -PnextfazeArtifactoryPassword="$MAVEN_PSW" \
                        || true
                    """
                }
            }
            post {
                always {
                    junit '**/test-results/**/*.xml'
                }
            }
        }
    }
}