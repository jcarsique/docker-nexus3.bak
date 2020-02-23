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

List<Map<String, String>> actionDetails = []
Map scriptResults = [changed: false, error: false]
scriptResults.put('action_details', actionDetails)

new JsonSlurper().parseText(args).each { taskDef ->

    String scriptName = taskDef.scriptName
    String repoName = taskDef.repoName
    String retained = taskDef.retained
    String retentionDays = taskDef.retentionDays
    String removeReleased = taskDef.removeReleased
    Integer gracePeriod = taskDef.gracePeriod
    String mail = taskDef.mail
    String cron = taskDef.cron

    Map<String, String> currentResult = [scriptName    : scriptName,
                                         repoName      : repoName,
                                         retained      : retained,
                                         retentionDays : retentionDays,
                                         removeReleased: removeReleased,
                                         gracePeriod   : gracePeriod,
                                         mail          : mail,
                                         cron          : cron]

    taskScheduler = container.lookup(TaskScheduler.class)
    TaskInfo existingTask = taskScheduler.listsTasks().find { TaskInfo taskInfo ->
        taskInfo.name == scriptName
    }
    if (existingTask) {
        log.debug("Task {} already exists.", scriptName)
        currentResult.put('status', 'exists')
    } else {
        try {
            taskConfiguration = taskScheduler.createTaskConfigurationInstance("repository.maven.remove-snapshots")
            taskConfiguration.name = scriptName
            taskConfiguration.setString("repositoryName", repoName)
            taskConfiguration.setString("minimumRetained", retained)
            taskConfiguration.setString("snapshotRetentionDays", retentionDays)
            taskConfiguration.setString("removeIfReleased", removeReleased)
            taskConfiguration.setInteger("gracePeriodInDays", gracePeriod)
            taskConfiguration.setAlertEmail(mail)
            Cron run8amEveryDay = taskScheduler.getScheduleFactory().cron(new Date(), cron);
            taskScheduler.scheduleTask(taskConfiguration, run8amEveryDay)
            currentResult.put('status', 'created')
            scriptResults['changed'] = true
        } catch (Exception e) {
            log.error('Could not create task {}: {}', scriptName, e.toString())
            currentResult.put('status', 'error')
            scriptResults['error'] = true
            currentResult.put('error_msg', e.toString())
        }
    }
    scriptResults['action_details'].add(currentResult)
}
return JsonOutput.toJson(scriptResults)
