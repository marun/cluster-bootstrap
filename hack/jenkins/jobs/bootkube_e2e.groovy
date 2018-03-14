// META
default_repo = "kubernetes-incubator/bootkube"

// CONFIG
org_whitelist = ['coreos', 'coreos-inc']
job_admins = ['colemickens', 'ericchiang', 'rithujohn191', 'rphillips']
user_whitelist = job_admins

// JOBS
network_providers = ['flannel', 'calico']
network_providers.each { np ->
  // Note: the "tku-" prefix is to differentiate "team-kube-upstream" jenkins job
  // statuses and triggers from the legacy jobs and triggers.
  job_name = "tku-bootkube-e2e-${np}"

  pipelineJob(job_name) {
    parameters {
      stringParam('sha1', 'origin/master', 'git reference to build')
      stringParam('repo', default_repo,    'git repo url to pull from')
    }
    definition {
      triggers {
        githubPullRequest {
          admins(job_admins)
          userWhitelist(user_whitelist)
          orgWhitelist(org_whitelist)
          useGitHubHooks(true)
          onlyTriggerPhrase(false)
          triggerPhrase("coreosbot run ${job_name}")

          extensions {
            commitStatus {
              context(job_name)
              triggeredStatus('e2e triggered')
              startedStatus('e2e started')
              completedStatus('SUCCESS', 'e2e succeeded')
              completedStatus('FAILURE', 'e2e failed. Investigate!')
              completedStatus('PENDING', 'e2e queued')
              completedStatus('ERROR', 'e2e internal error. Investigate!')
            }
          }
        }
      }

      cpsScm {
        scm {
          git {
            remote {
              github('${repo}')
              refspec('+refs/heads/*:refs/remotes/origin/*')
              credentials('github_userpass')
            }
            branch('${sha1}')
          }
        }
        scriptPath('hack/jenkins/pipelines/bootkube-e2e/Jenkinsfile')
      }
    }
  }
}
