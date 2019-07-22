import org.sonatype.nexus.script.plugin.RepositoryApi;
import groovy.json.JsonOutput;
import org.sonatype.nexus.script.plugin.internal.provisioning.RepositoryApiImpl;
import groovy.json.JsonSlurper;
import org.sonatype.nexus.repository.storage.WritePolicy;

def createHosted(String name, String type, String blobstore, Map repoDef) {
  log.info("Create hosted repository {}", name)
  if (type == "maven") {
    repository.createMavenHosted(name, blobstore);
  } else if (type == "npm") {
    repository.createNpmHosted(name, blobstore)
  } else if (type == "nuget") {
    repository.createNugetHosted(name, blobstore);
  } else if (type == "raw") {
    repository.createRawHosted(name, blobstore);
  } else if (type == "bower") {
      repository.createBowerHosted(name, blobstore);
  } else if (type == "docker") {
      repository.createDockerHosted(name, repoDef.httpPort, repoDef.httpsPort, blobstore, true, true, WritePolicy.ALLOW, false)
  }
}

def createProxy(String name, String type, String blobstore, String url, Map repoDef) {
  if (url == null) {
      throw new Exception("Missing proxy URL for {}", name)
  }
  log.info("Create proxy repository {}", name)
  if (type == "maven") {
    repository.createMavenProxy(name, url, blobstore)
  } else if (type == "npm") {
    repository.createNpmProxy(name, url, blobstore)
  } else if (type == "nuget") {
    repository.createNugetProxy(name, url, blobstore)
  } else if (type == "raw") {
    repository.createRawProxy(name, url, blobstore)
  } else if (type == "bower") {
    repository.createBowerProxy(name, url, blobstore)
  } else if (type == "docker") {
      repository.createDockerProxy(name, url, repoDef.indexType, repoDef.indexUrl, repoDef.httpPort, repoDef.httpsPort, blobstore)
  }
  //repository.getRepositoryManager().get(name).getConfiguration().getAttributes().'proxy'.'contentMaxAge' = contentMaxAge
}

def createGroup(String name, String type, List<String> members, String blobstore) {
  log.info("Create group repository {}", name)
  if (type == "maven") {
    repository.createMavenGroup(name, members, blobstore)
  } else if (type == "npm") {
    repository.createNpmGroup(name, members, blobstore)
  } else if (type == "nuget") {
    repository.createNugetGroup(name, members, blobstore)
  } else if (type == "raw") {
    repository.createRawGroup(name, members, blobstore)
  } else if (type == "bower") {
    repository.createBowerGroup(name, members, blobstore)
  }
}

List<Map<String, String>> actionDetails = []
Map scriptResults = [changed: false, error: false]
scriptResults.put('action_details', actionDetails)
new JsonSlurper().parseText(args).each { repoDef ->
    /**
    * JSON repository definition
    * @param name The name of the new Repository
    * @param type The type of the Repository (maven / npm / nuget / raw / bower / docker)
    * @param hostedtype The hosting type of the Repository (hosted / proxy / group)
    * @param blobStore The name of the BlobStore the Repository should use
    * @param url /OPTIONAL/ Only used with hostedtype == proxy, The URL the repository points to
    * @param members /OPTIONAL/ Only used with hostedtype == group, The list of Repositories in the group
    * @param httpPort /OPTIONAL/ Only used with type == docker
    * @param httpsPort /OPTIONAL/ Only used with type == docker
    * @param indexType /OPTIONAL/ Only used with type == docker and hostedtype == proxy
    * @param indexUrl /OPTIONAL/ Only used with type == docker and hostedtype == proxy
    */
    Map<String, String> currentResult = [name: repoDef.name, type: repoDef.type, hostedtype: repoDef.hostedtype,
        blobstore: repoDef.blobstore]

    RepositoryApiImpl api = (RepositoryApiImpl) repository
    existingRepo = api.getRepositoryManager().get(repoDef.name)
    if (existingRepo == null) {
        try {
            if (repoDef.hostedtype == "hosted") {
                createHosted(repoDef.name, repoDef.type, repoDef.blobstore, repoDef)
            }  else if (repoDef.hostedtype == "proxy") {
                createProxy(repoDef.name, repoDef.type, repoDef.blobstore, repoDef.url, repoDef)
            } else if (repoDef.hostedtype == "group") {
                if (repoDef.members == null) {
                    log.warn("Repository group {} is empty", repoDef.name);
                }
                createGroup(repoDef.name, repoDef.type, repoDef.members, repoDef.blobstore)
            }
            currentResult.put('status', 'created')
            scriptResults['changed'] = true
        } catch (Exception e) {
            log.error('Could not create repository {}: {}', repoDef.name, e.toString())
            currentResult.put('status', 'error')
            scriptResults['error'] = true
            currentResult.put('error_msg', e.toString())
        }
    } else {
        log.info("Repository {} already exists. Left untouched", repoDef.name)
        currentResult.put('status', 'exists')
    }
    scriptResults['action_details'].add(currentResult)
}
return JsonOutput.toJson(scriptResults)
