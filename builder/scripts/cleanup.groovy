import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.sonatype.nexus.scheduling.schedule.Cron;
import org.sonatype.nexus.capability.CapabilityRegistry
import org.sonatype.nexus.repository.storage.WritePolicy
import org.sonatype.nexus.security.user.UserSearchCriteria
import org.sonatype.nexus.security.authc.apikey.ApiKeyStore
import org.sonatype.nexus.security.realm.RealmManager;
import org.apache.shiro.subject.SimplePrincipalCollection
import org.sonatype.nexus.scheduling.TaskScheduler
import org.sonatype.nexus.scheduling.schedule.Daily
import org.sonatype.nexus.security.realm.RealmConfiguration
import org.sonatype.nexus.scheduling.TaskInfo
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


taskScheduler = (TaskScheduler)container.lookup(TaskScheduler.class.name)

TaskInfo existingTask = taskScheduler.listsTasks().find { TaskInfo taskInfo ->
  taskInfo.name == "cleanupOldSnapshots"
}

if (existingTask) {
  log.info("Task already exists .... quitting script execution")
  return
}

taskConfiguration = taskScheduler.createTaskConfigurationInstance("repository.maven.remove-snapshots")
taskConfiguration.name = "cleanupOldSnapshots"
taskConfiguration.setString("repositoryName", "*")
taskConfiguration.setString("minimumRetained", "2")
taskConfiguration.setString("snapshotRetentionDays", "90")
taskConfiguration.setString("removeIfReleased", "true")
taskConfiguration.setInteger("gracePeriodInDays", 7)
taskConfiguration.setAlertEmail("devtools+packages@nuxeo.com")
Cron run8amEveryDay = taskScheduler.getScheduleFactory().cron(new Date(), "0 0 8 * * ?");
taskScheduler.scheduleTask(taskConfiguration, run8amEveryDay)

option_helper =  taskScheduler.taskFactory.descriptors.collect { [
        id: it.id,
        name: it.name,
        exposed: it.exposed,
        formFields: it.formFields?.collect { [
            id: it.id,
            type: it.type,
            label: it.label,
            helpText: it.helpText,
            required: it.required,
            regexValidation: it.regexValidation,
            initialValue: it.initialValue,
            ] }
] }
log.info(JsonOutput.toJson(option_helper))
