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
                    gradlew('clean assemble')
                }
            }
        }

        stage('Lint') {
            steps {
                gitlabCommitStatus(name: 'lint') {
                    gradlew('lint')
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
                gitlabCommitStatus(name: 'test') {
                    gradlew('--continue test || true')
                }
            }
            post {
                always {
                    junit '**/test-results/**/*.xml'
                    script {
                        if (currentBuild.result == 'UNSTABLE') {
                            updateGitlabCommitStatus name: "Test", state: 'failed'
                        }
                    }
                }
            }
        }
    }
}

/** Runs the Gradle wrapper with the specified command and some other common arguments. */
def gradlew(command) {
    // Always disable Kotlin daemon and incremental compilation.
    // The daemon can fails sporadically, and incremental could introduce output errors
    sh """./gradlew \\
        -Dkotlin.compiler.execution.strategy='in-process' \\
        -Pkotlin.incremental=false \\
        -PnextfazeArtifactoryUser="$MAVEN_USR" \\
        -PnextfazeArtifactoryPassword="$MAVEN_PSW" \\
        $command
    """.trim()
}
