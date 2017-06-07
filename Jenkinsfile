pipeline {
    options {
        gitLabConnection('gitlab.nextfaze.com')
    }
    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }

    agent { label 'android' }

    stages {
        stage('Assemble') {
            steps {
                withCredentials([[
                    $class: 'UsernamePasswordMultiBinding',
                    credentialsId: 'jenkins-maven',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'PASSWORD'
                ]]) {
                    gitlabCommitStatus(name: 'build') {
                        sh "./gradlew clean daggie:assemble -PnextfazeArtifactoryUser=$USERNAME -PnextfazeArtifactoryPassword=$PASSWORD"
                    }
                }
            }
        }

        stage('Lint') {
            steps {
                withCredentials([[
                    $class: 'UsernamePasswordMultiBinding',
                    credentialsId: 'jenkins-maven',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'PASSWORD'
                ]]) {
                    gitlabCommitStatus(name: 'lint') {
                        sh "./gradlew daggie:lint -PnextfazeArtifactoryUser=$USERNAME -PnextfazeArtifactoryPassword=$PASSWORD"
                    }
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
                withCredentials([[
                    $class: 'UsernamePasswordMultiBinding',
                    credentialsId: 'jenkins-maven',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'PASSWORD'
                ]]) {
                    gitlabCommitStatus(name: "test") {
                        sh "./gradlew --continue daggie:test -PnextfazeArtifactoryUser=$USERNAME -PnextfazeArtifactoryPassword=$PASSWORD || true"
                    }
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