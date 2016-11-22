import dsl.jobBuilder.BaseJobBuilder
import dsl.jobBuilder.Utils.Steps
import dsl.jobBuilder.Utils.Scm
import static dsl.jobBuilder.Utils.Param.requiredString

String directory = 'Job-examples-1'
String repository = 'file:///home/vagrant/centos-jenkins-dsl/'

folder(directory) {
    description 'Contains simple job examples'
}

job("$directory/built-simple") {
    logRotator(2, 10, -1, -1)
    scm {
        git {
            remote {
                url repository
                branch 'master'
            }
        }
    }
    parameters {
        booleanParam 'NOTIFY_QA', true, ""
    }
    configure { project -> 
        project / 'properties' / 'hudson.model.ParametersDefinitionProperty' / parameterDefinitions << 'hudson.plugins.validating__string__parameter.ValidatingStringParameterDefinition' {
            name('IMPORTANT_GIT_TAG')
            defaultValue(null)
            failedValidationMessage('Warning!')
            description('''
                Some big description of this parameter
                '''.stripIndent().trim()
            )
        }
    }
    steps {
        shell(readFileFromWorkspace('jobs/scripts/test.sh'))
        gradle {
            useWrapper true
            tasks 'clean test'
            switches '''
                -Dhttp.proxyHost=proxy.example.com
                -Dhttps.proxyHost=proxy.example.com
                -Dhttp.proxyPort=80
                -Dhttps.proxyPort=80
            '''.stripIndent().trim()
        }
    }
    publishers {
        chucknorris()
    }
}

job("$directory/built-with-utils") {
    logRotator(2, 10, -1, -1)
    scm {
        Scm.git(delegate,repository,'master')
    }
    parameters {
        booleanParam 'NOTIFY_QA', false, ""
    }
    configure requiredString('IMPORTANT_GIT_TAG')
    steps {
        shell(readFileFromWorkspace('jobs/scripts/test.sh'))
        Steps.proxiedGradle(delegate, 'clean test')
    }
    publishers {
        chucknorris()
    }
}

new BaseJobBuilder(
    directory: directory,
    name: "built-with-class",
    repository: repository,
    branch: 'master',
    gitTag: 'IMPORTANT_GIT_TAG',
    script: 'test.sh',
    gradleTasks: 'clean test'
).build(this)