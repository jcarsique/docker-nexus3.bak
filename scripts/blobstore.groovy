import org.sonatype.nexus.blobstore.api.BlobStoreConfiguration

import groovy.json.JsonOutput
import groovy.json.JsonSlurper


List<Map<String, String>> actionDetails = []
Map scriptResults = [changed: false, error: false]
scriptResults.put('action_details', actionDetails)
msg = ""

new JsonSlurper().parseText(args).each { blobstoreDef ->
    /**
     * JSON BlobStore definition.
     *
     * @param name the name for the new BlobStore
     * @param path the File BlobStore data path
     * @param config the S3 BlobStore config
     */
    Map<String, String> currentResult = [name: blobstoreDef.name, type: blobstoreDef.get('type', 'file')]

    existingBlobStore = blobStore.getBlobStoreManager().get(blobstoreDef.name)
    if (existingBlobStore == null) {
        try {
            if (blobstoreDef.type == "S3") {
                log.info("Create S3 blobstore {}", blobstoreDef.name)
                blobStore.createS3BlobStore(blobstoreDef.name, blobstoreDef.config)
            } else {
                log.info("Create File blobstore {}", blobstoreDef.name)
                blobStore.createFileBlobStore(blobstoreDef.name, blobstoreDef.path)
            }
            currentResult.put('status', 'created')
            scriptResults['changed'] = true
        } catch (Exception e) {
            log.error('Could not create blobstore {}: {}', blobstoreDef.name, e)
            currentResult.put('status', 'error')
            scriptResults['error'] = true
            currentResult.put('error_msg', e.toString())
        }
    } else {
        msg = "Blobstore {} already exists. Left untouched"
        currentResult.put('status', 'exists')
    }

    log.info(msg, blobstoreDef.name)
    scriptResults['action_details'].add(currentResult)
}

return JsonOutput.toJson(scriptResults)
