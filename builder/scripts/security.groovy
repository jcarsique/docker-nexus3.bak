import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.user.UserSearchCriteria;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.role.NoSuchRoleException;
import org.sonatype.nexus.security.realm.RealmManager;
import org.sonatype.nexus.security.role.RoleIdentifier;
import java.util.Set;
import java.util.stream.Collectors;
import static org.sonatype.nexus.security.user.UserManager.DEFAULT_SOURCE

nexusRealms = ["NexusAuthenticatingRealm",
               "NexusAuthorizingRealm",
               "NuGetApiKey",
               "User-Token-Realm",
               "com.larscheidschmitzhermes.nexus3.github.oauth.plugin.GithubOauthAuthenticatingRealm",
               "NpmToken",
               "DockerToken",
               "rutauth-realm"]

realmManager = container.lookup(RealmManager.class.getName())
nexusRealms.each {
    if (!(realmManager.isRealmEnabled(it))) {
        realmManager.enableRealm(it, true)
    }
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

def updateUser(Map argsLine) {
    def user = security.securitySystem.getUser(argsLine.id)
    log.info("Update user {}", user.toString())
    user.setFirstName(argsLine.firstname)
    user.setLastName(argsLine.lastname)
    user.setEmailAddress(argsLine.mail)
    user.setRoles(argsLine.roles.collect { new RoleIdentifier(DEFAULT_SOURCE, it) }.toSet())
    security.securitySystem.updateUser(user)
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

/*
 * Main execution
 */

try {
    config = (new JsonSlurper()).parseText(args)
} catch (Exception e) {
    throw new MyException("Configuration is not valid. {}", e)
}

/**
 * Create a Nexus User or Role
 * type="user", see #createUser()
 * type="role", see #createRole()
 * type="anonymous": whether to activate anonymous access
 */
config.each { argsLine ->
    log.debug("argsLine: {}", argsLine)

    try {
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
         * @param roles List of roles (ie: nx-anonymous / nx-admin)
         */
        Map<String, String> roleResult = [type: argsLine.type, id: argsLine.id, name: argsLine.name,
                                          description: argsLine.description, privileges: argsLine.privileges, roles: argsLine.roles]
    } catch (Exception e) {
        log.error("Configuration is not valid. {}", e)
    }

    if (argsLine.type == "user") {
        try {
            users = security.securitySystem.searchUsers(new UserSearchCriteria(argsLine.id))
            if (users.isEmpty()) {
                createUser(argsLine, passwords[argsLine.id])
                userResult.put('status', 'created')
            } else {
                // password changes are ignored for now
                updateUser(argsLine)
                userResult.put('status', 'updated')
            }
        } catch (Exception e) { // TODO better exception handling
            log.error('Could not create user {}: {}', argsLine.id, e.toString())
            userResult.put('status', 'error')
            userResult.put('error_msg', e.toString())
            scriptResults['error'] = true
        }
        scriptResults['action_details'].add(userResult)
    } else if (argsLine.type == "role") {
        try {
            try {
                Role role = security.securitySystem.getAuthorizationManager("default").getRole(argsLine.id)
                log.info("Role {} already exists. Left untouched", argsLine.id)
                roleResult.put('status', 'exists')
            } catch (NoSuchRoleException e) {
                createRole(argsLine)
            }
        } catch (Exception e) { // TODO better exception handling
            log.error('Could not create role {}: {}', argsLine.id, e.toString())
            roleResult.put('status', 'error')
            roleResult.put('error_msg', e.toString())
            scriptResults['error'] = true
        }
        scriptResults['action_details'].add(roleResult)
    } else if (argsLine.type == "anonymous") {
        security.setAnonymousAccess(argsLine.enabled)
        log.info('Anonymous access {}', argsLine.enabled)
    }
}

return JsonOutput.toJson(scriptResults)
