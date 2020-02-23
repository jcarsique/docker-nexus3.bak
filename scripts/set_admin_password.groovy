import org.sonatype.nexus.security.SecuritySystem;

String pass = new File('/opt/sonatype/nexus/config/password')?.text
if (pass) {
    security.securitySystem.changePassword('admin', pass.trim())
} else {
    log.warn("No admin password file! Default password is unchanged.")
}

