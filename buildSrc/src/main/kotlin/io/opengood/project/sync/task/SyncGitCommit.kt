package io.opengood.project.sync.task

import com.lordcodes.turtle.shellRun
import io.opengood.project.sync.model.SyncProject
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class SyncGitCommit : BaseTask() {

    @Input
    lateinit var workspacePath: String

    @Input
    lateinit var projectPath: String

    @Input
    lateinit var commitMessage: String

    init {
        group = "sync"
        description = "Performs Git commit and push containing sync changes for each project"
    }

    @TaskAction
    fun run() {
        execute(
            taskName = TASK_NAME,
            displayName = TASK_DISPLAY_NAME,
            workspacePath = workspacePath,
            projectPath = projectPath
        ) { _, _, project: SyncProject, _ ->
            with(project) {
                shellRun(dir) {
                    with(project.git) {
                        printInfo("Determining project changes for '${name}' in local Git repo '${dir}'...")
                        val status = git.status()

                        if (status.isNotBlank()) {
                            printInfo("Git status:")
                            printInfo(status)

                            printProgress("Checking out '$branch' branch on local Git repo...")
                            git.checkout(branch)

                            printProgress("Committing all changes to '$branch' branch in local Git repo...")
                            git.commitAllChanges(commitMessage)
                            printDone()

                            printProgress("Pulling potential changes from remote '$remote' Git repo...")
                            git.pull(remote, branch)
                            printDone()

                            printProgress("Pushing changes to remote '$remote' Git repo...")
                            git.push(remote, branch)
                            printDone()
                        } else {
                            printInfo("No project changes found in local Git repo. Skipping.")
                        }
                    }
                    ""
                }
            }
        }
    }

    companion object {
        const val TASK_NAME = "syncGitCommit"
        const val TASK_DISPLAY_NAME = "Sync Git Commit"
    }
}