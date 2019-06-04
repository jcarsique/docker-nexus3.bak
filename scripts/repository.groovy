import org.sonatype.nexus.script.plugin.RepositoryApi
import groovy.json.JsonOutput
import org.sonatype.nexus.script.plugin.internal.provisioning.RepositoryApiImpl

import groovy.json.JsonSlurper

parsed_args = new JsonSlurper().parseText(args)

List<Map<String, String>> actionDetails = []
Map scriptResults = [changed: false, error: false]
scriptResults.put('action_details', actionDetails)
msg = ""

def createHosted(String name, String type, String blobstore) {

	if (type == "maven") {
	  repository.createMavenHosted(name, blobstore);
	}
	if (type == "npm") {
	  repository.createNpmHosted(name, blobstore)
	}
	if (type == "nuget") {
	  repository.createNugetHosted(name, blobstore);
	}
	if (type == "raw") {
	  repository.createRawHosted(name, blobstore);
	}
	if (type == "bower") {
      repository.createBowerHosted(name, blobstore);
	}
}

def createProxy(String name, String type, String blobstore, String url) {

	if (type == "maven") {
	  repository.createMavenProxy(name, url, blobstore)
	}
	if (type == "npm") {
	  repository.createNpmProxy(name, url, blobstore)
	}
	if (type == "nuget") {
	  repository.createNugetProxy(name, url, blobstore)
	}
	if (type == "raw") {
	  repository.createRawProxy(name, url, blobstore)
	}
	if (type == "bower") {
	  repository.createBowerProxy(name, url, blobstore)
	}
	//repository.getRepositoryManager().get(name).getConfiguration().getAttributes().'proxy'.'contentMaxAge' = contentMaxAge
}

def createGroup(String name, String type, List<String> members, String blobstore) {

	if (type == "maven") {
	  repository.createMavenGroup(name, members, blobstore)
	}
	if (type == "npm") {
	  repository.createNpmGroup(name, members, blobstore)
	}
	if (type == "nuget") {
	  repository.createNugetGroup(name, members, blobstore)
	}
	if (type == "raw") {
	  repository.createRawGroup(name, members, blobstore)
	}
	if (type == "bower") {
	  repository.createBowerGroup(name, members, blobstore)
	}
}

parsed_args.each { repoDef ->

	/**
	 * Create a Nexus repository.
	 * @param name The name of the new Repository
	 * @param type The type of the Repository (maven / npm / nuget / raw / bower)
	 * @param hostedtype The hosting type of the Repository (hosted / proxy / group)
	 * @param blobStore The name of the BlobStore the Repository should use
	 * @param url /OPTIONAL/ Only used with hostedtype == proxy, The URL the repository points to
	 * @param members /OPTIONAL/ Only used with hostedtype == group, The list of Repositories in the group
	 */

    Map<String, String> currentResult = [name: repoDef.name, \
                                         type: repoDef.type, \
                                         hostedtype: repoDef.hostedtype, \
                                         blobstore: repoDef.blobstore]

    RepositoryApiImpl api = (RepositoryApiImpl)repository

    existingRepo = api.getRepositoryManager().get(repoDef.name)
    if (existingRepo == null) {
        try {

            if (repoDef.hostedtype == "hosted") {

                createhosted(repodef.name, repodef.type, repodef.blobstore)
                msg = "hosted repository {} created"
            }  else if (repoDef.hostedtype == "proxy") {
                if (repoDef.url == null) {
                    // Check if URL is set when we define a Proxy repository
                    msg = "Proxy URL not provided, exiting script on error";
                    log.info(msg, repoDef.name);
                    currentResult.put('status', 'error');
                }
                createProxy(repoDef.name, repoDef.type, repoDef.blobstore, repoDef.url)
                msg = "Proxy repository {} created"
            } else if (repoDef.hostedtype == "group") {
                if (repoDef.members == null) {
                    // Check if repository members are set when we define a Group repository
                    msg = "Repository members not provided, exiting script on error";
                    log.info(msg, repoDef.name);
                    currentResult.put('status', 'error');
                }
                createGroup(repoDef.name, repoDef.type, repoDef.members, repoDef.blobstore)
                msg = "Group repository {} created"
            }
            log.info(msg, repoDef.name)
            currentResult.put('status', 'created')
            scriptResults['changed'] = true
        } catch (Exception e) {
            log.error('Could not create repository {}: {}', repoDef.name, e.toString())
            currentResult.put('status', 'error')
            scriptResults['error'] = true
            currentResult.put('error_msg', e.toString())
        }
    } else {
        msg = "Repository {} already exists. Left untouched"
        currentResult.put('status', 'exists')
    }
    log.info(msg, repoDef.name)
    scriptResults['action_details'].add(currentResult)
}

return JsonOutput.toJson(scriptResults)
