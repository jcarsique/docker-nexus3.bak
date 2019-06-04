import org.sonatype.nexus.blobstore.api.BlobStoreConfiguration

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

parsed_args = new JsonSlurper().parseText(args)

List<Map<String, String>> actionDetails = []
Map scriptResults = [changed: false, error: false]
scriptResults.put('action_details', actionDetails)
msg = ""

/**
 * Create a new File or S3 based BlobStore.
 *
 * @param name the name for the new BlobStore
 * @param path the path where the BlobStore should store data (File)
 * @param config the configuration map for the new blobstore (S3)
 */
parsed_args.each { blobstoreDef ->

    Map<String, String> currentResult = [name: blobstoreDef.name, type: blobstoreDef.get('type', 'file')]

    existingBlobStore = blobStore.getBlobStoreManager().get(blobstoreDef.name)
    if (existingBlobStore == null) {
        try {
            if (blobstoreDef.type == "S3") {
                blobStore.createS3BlobStore(blobstoreDef.name, blobstoreDef.config)
                msg = "S3 blobstore {} created"
            } else {
                blobStore.createFileBlobStore(blobstoreDef.name, blobstoreDef.path)
                msg = "File blobstore {} created"
            }
            log.info(msg, blobstoreDef.name)
            currentResult.put('status', 'created')
            scriptResults['changed'] = true
        } catch (Exception e) {
            log.error('Could not create blobstore {}: {}', blobstoreDef.name, e.toString())
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
