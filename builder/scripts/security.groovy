import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.user.UserSearchCriteria;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.role.NoSuchRoleException;
import org.sonatype.nexus.security.realm.RealmManager;


realmManager = container.lookup(RealmManager.class.getName())
if (!(realmManager.isRealmEnabled("DockerToken"))) {
    realmManager.enableRealm("DockerToken", true)
}

// Admin password
String pass = new File('/opt/sonatype/nexus/config/password')?.text
if (pass) {
    security.securitySystem.changePassword('admin', pass.trim())
} else {
    log.warn("No admin password file! Default password is unchanged.")
}

List<Map<String, String>> actionDetails = []
Map scriptResults = [changed: false, error: false]
scriptResults.put('action_details', actionDetails)
created = 0

def createUser(Map argsLine, password) {
    log.info("Create user {}", argsLine.id)
    security.addUser(argsLine.id, argsLine.firstname, argsLine.lastname, argsLine.mail, true, password, argsLine.roles)
}

def createRole(Map argsLine) {
    log.info("Create role {}", argsLine.id)
    security.addRole(argsLine.id, argsLine.name, argsLine.description, argsLine.privileges, argsLine.roles)
}

pwdFile = new File('/opt/sonatype/nexus/config/passwords.json')
if (pwdFile.exists()) {
    passwords = new JsonSlurper().parseText(pwdFile.text)
} else {
    passwords = [:]
}

/**
 * Create a Nexus User or Role
 * type="user", see #createUser()
 * type="role", see #createRole()
 * type="anonymous": whether to activate anonymous access
 */
new JsonSlurper().parseText(args).each { argsLine ->
    /**
     * JSON user definition
     * @param id ID of the user
     * @param firstname Firstname of the user
     * @param lastname Lastname of the user
     * @param mail Mail of the user
     * @param password Password of the user
     * @param roles Roles to be associated with the user (List<String>)
     */
    Map<String, String> userResult = [type: argsLine.type, id: argsLine.id, firstname: argsLine.firstname,
        lastname: argsLine.lastname, mail: argsLine.mail, roles: argsLine.roles]

    /**
     * JSON role definition
     * @param id ID of the role
     * @param name Name of the role
     * @param description Description of the role
     * @param privileges List of privileges (ie: nx-healthcheck-read)
     * @param roles List of inherithed roles (ie: nx-anonymous / nx-admin)
     */
    Map<String, String> roleResult = [type: argsLine.type, id: argsLine.id, name: argsLine.name,
        description: argsLine.description, privileges: argsLine.privileges, roles: argsLine.roles]

    if (argsLine.type == "user") {
        try {
            users = security.getSecuritySystem().searchUsers(new UserSearchCriteria(argsLine.id))
            if (users.isEmpty())
                createUser(argsLine, passwords[argsLine.id])
            else {
                userResult.put('status', 'exists')
                log.info("User {} already exists. Left untouched", argsLine.id)
            }
            scriptResults['action_details'].add(userResult)
        } catch (Exception e) { // TODO better exception handling
            log.error('Could not create user {}: {}', argsLine.id, e.toString())
            userResult.put('status', 'error')
            userResult.put('error_msg', e.toString())
            scriptResults['error'] = true
            scriptResults['action_details'].add(userResult)
        }
    } else if (argsLine.type == "role") {
        try {
            try {
                Role role = security.getSecuritySystem().getAuthorizationManager("default").getRole(argsLine.id)
                log.info("Role {} already exists. Left untouched", argsLine.id)
                roleResult.put('status', 'exists')
            } catch (NoSuchRoleException e) {
                createRole(argsLine)
            }
            scriptResults['action_details'].add(roleResult)
        } catch (Exception e) { // TODO better exception handling
            log.error('Could not create role {}: {}', argsLine.id, e.toString())
            roleResult.put('status', 'error')
            roleResult.put('error_msg', e.toString())
            scriptResults['error'] = true
            scriptResults['action_details'].add(roleResult)
        }
    } else if (argsLine.type == "anonymous") {
        security.setAnonymousAccess(argsLine.enabled)
        log.info('Anonymous access {}', argsLine.enabled)
    }
}

return JsonOutput.toJson(scriptResults)
