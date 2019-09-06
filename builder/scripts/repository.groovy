import org.sonatype.nexus.script.plugin.RepositoryApi
import groovy.json.JsonOutput
import org.sonatype.nexus.script.plugin.internal.provisioning.RepositoryApiImpl
import groovy.json.JsonSlurper
import org.sonatype.nexus.repository.storage.WritePolicy
import org.sonatype.nexus.repository.maven.VersionPolicy
import org.sonatype.nexus.repository.maven.LayoutPolicy


def createHosted(Map repoDef) {
    String name = repoDef.name
    String type = repoDef.type
    String blobstore = repoDef.blobstore
    String httpPort = repoDef.httpPort
    String httpsPort = repoDef.httpsPort
    VersionPolicy versionPolicy = VersionPolicy.valueOf(repoDef.versionPolicy)
    log.info("Create hosted repository {}", name)
    if (type == "maven") {
        repository.createMavenHosted(name, blobstore, true, versionPolicy, WritePolicy.ALLOW, LayoutPolicy.STRICT)
    } else if (type == "npm") {
        repository.createNpmHosted(name, blobstore)
    } else if (type == "nuget") {
        repository.createNugetHosted(name, blobstore)
    } else if (type == "raw") {
        repository.createRawHosted(name, blobstore)
    } else if (type == "bower") {
        repository.createBowerHosted(name, blobstore)
    } else if (type == "docker") {
        repository.createDockerHosted(name, httpPort, httpsPort, blobstore, true, true, WritePolicy.ALLOW, false)
    }
}

def createProxy(Map repoDef) {
    String name = repoDef.name
    String type = repoDef.type
    String blobstore = repoDef.blobstore
    String url = repoDef.url
    String indexType = repoDef.indexType
    String indexUrl = repoDef.indexUrl ? repodef.indexUrl : repoDef.url;
    VersionPolicy versionPolicy = VersionPolicy.valueOf(repoDef.versionPolicy)
    Integer httpPort = repoDef.httpPort ? new Integer(repoDef.httpPort) : null;
    Integer httpsPort = repoDef.httpsPort ? new Integer(repoDef.httpsPort) : null;
    if (url == null) {
        throw new Exception("Missing proxy URL for {}", name)
    }
    log.info("Create proxy repository {}", name)
    if (type == "maven") {
        repository.createMavenProxy(name, url, blobstore, true, versionPolicy, LayoutPolicy.STRICT)
    } else if (type == "npm") {
        repository.createNpmProxy(name, url, blobstore)
    } else if (type == "nuget") {
        repository.createNugetProxy(name, url, blobstore)
    } else if (type == "raw") {
        repository.createRawProxy(name, url, blobstore)
    } else if (type == "bower") {
        repository.createBowerProxy(name, url, blobstore)
    } else if (type == "docker") {
        repository.createDockerProxy(name, url, indexType, indexUrl, httpPort, httpsPort, blobstore, true, true)
    }
    // repository.getRepositoryManager().get(name).getConfiguration().getAttributes().'proxy'.'contentMaxAge' = contentMaxAge
}

def createGroup(Map repoDef) {
    String name = repoDef.name
    String type = repoDef.type
    List<String> members = repoDef.members
    String blobstore = repoDef.blobstore
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

/**
 * JSON repository definition
 * @param name The name of the new Repository
 * @param type The type of the Repository (maven / npm / nuget / raw / bower / docker)
 * @param hostedtype The hosting type of the Repository (hosted / proxy / group)
 * @param blobstore The name of the blob store the Repository should use
 * @param url /OPTIONAL/ Only used with hostedtype == proxy, The URL the repository points to
 * @param members /OPTIONAL/ Only used with hostedtype == group, The list of Repositories in the group
 * @param httpPort /OPTIONAL/ Only used with type == docker
 * @param httpsPort /OPTIONAL/ Only used with type == docker
 * @param indexType /OPTIONAL/ Only used with type == docker and hostedtype == proxy
 * @param indexUrl /OPTIONAL/ Only used with type == docker and hostedtype == proxy
 */
new JsonSlurper().parseText(args).each { repoDef ->
    String name = repoDef.name
    String type = repoDef.type
    String hostedtype = repoDef.hostedtype
    String blobstore = repoDef.blobstore

    Map<String, String> currentResult = [name: name, type: type, hostedtype: hostedtype, blobstore: blobstore]

    RepositoryApiImpl api = (RepositoryApiImpl) repository
    existingRepo = api.getRepositoryManager().get(name)
    if (existingRepo == null) {
        try {
            if (hostedtype == "hosted") {
                createHosted(repoDef)
            }  else if (hostedtype == "proxy") {
                createProxy(repoDef)
            } else if (hostedtype == "group") {
                if (repoDef.members == null) {
                    log.warn("Repository group {} is empty", name)
                }
                createGroup(repoDef)
            }
            currentResult.put('status', 'created')
            scriptResults['changed'] = true
        } catch (Exception e) {
            log.error('Could not create repository {}: {}', name, e.toString())
            currentResult.put('status', 'error')
            scriptResults['error'] = true
            currentResult.put('error_msg', e.toString())
        }
    } else {
        log.info("Repository {} already exists. Left untouched", name)
        currentResult.put('status', 'exists')
    }
    scriptResults['action_details'].add(currentResult)
}
return JsonOutput.toJson(scriptResults)
