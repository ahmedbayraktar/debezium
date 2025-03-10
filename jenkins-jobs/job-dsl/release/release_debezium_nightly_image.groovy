// Job definition to test PostgreSQL connector against different PostgreSQL versions
folder("release") {
    description("This folder contains all jobs used by developers for upstream release and all relevant stuff")
    displayName("Release")
}

freeStyleJob('release/release-debezium-nightly-image') {

    displayName('Debezium Nightly Image')
    description('Build and deploy nightly image to the registry')
    label('Slave')

    properties {
        githubProjectUrl('https://github.com/debezium/container-images')
    }

    logRotator {
        daysToKeep(7)
        numToKeep(10)
    }

    wrappers {
        timeout {
            noActivity(600)
        }
        credentialsBinding {
            usernamePassword('DOCKER_USERNAME', 'DOCKER_PASSWORD', 'debezium-dockerhub')
        }
        credentialsBinding {
            string('QUAYIO_CREDENTIALS', 'debezium-quay')
        }
    }

    triggers {
        upstream('release-deploy_snapshots_pipeline')
    }

    publishers {
        mailer('jpechane@redhat.com', false, true)
    }

    parameters {
        stringParam('DEBEZIUM_REPOSITORY', 'debezium/debezium', 'Repository from which Debezium is built')
        stringParam('DEBEZIUM_BRANCH', 'main', 'Branch used to build Debezium')
        stringParam('IMAGES_REPOSITORY', 'https://github.com/debezium/container-images.git', 'Repository with Debezium Dockerfiles')
        stringParam('IMAGES_BRANCH', 'main', 'Branch used for images repository')
        stringParam('DEFAULT_PLATFORMS', 'linux/amd64,linux/arm64', 'Platform to build')
    }

   scm {
        git('$IMAGES_REPOSITORY', '$IMAGES_BRANCH')
    }

    steps {
        shell(readFileFromWorkspace('jenkins-jobs/scripts/trigger-nightly-docker.sh'))
    }
}
