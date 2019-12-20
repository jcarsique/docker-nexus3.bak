/*
 * (C) Copyright ${year} Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     - Alexis Timic <atimic@nuxeo.com>>
 */





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
import org.sonatype.nexus.scheduling.schedule.Schedule;

taskScheduler = container.lookup(TaskScheduler.class)
Schedule manualSchedule = taskScheduler.scheduleFactory.manual()

new JsonSlurper().parseText(args).each { taskDef ->

    String scriptName = taskDef.scriptName
    String mail = taskDef.mail

    TaskInfo existingTask = taskScheduler.listsTasks().find { TaskInfo taskInfo ->
        taskInfo.name == scriptName
    }
    if (existingTask) {
        log.debug("Task {} already exists.", scriptName)
    } else {
        try {
            taskConfiguration = taskScheduler.createTaskConfigurationInstance("script")
            taskConfiguration.name = scriptName
            taskConfiguration.setTypeName("Admin - Execute script")
            taskConfiguration.setAlertEmail(mail)
            taskConfiguration.setString("source", "core.connectionTimeout(300);")
            taskConfiguration.setString("schedule", "manual")
            taskConfiguration.setString("language", "groovy")
            taskConfiguration.setString("multinode", "true")
            taskConfiguration.setString("visible", "true")
            taskConfiguration.setEnabled(true)
            taskScheduler.scheduleTask(taskConfiguration, manualSchedule)
            id = taskConfiguration.getId()
            taskScheduler.getTaskById(id).runNow()
            taskConfiguration.setString("method", "run")
        } catch (Exception e) {
            log.error('Could not create task {}: {}', scriptName, e.toString())
        }
    }
}
