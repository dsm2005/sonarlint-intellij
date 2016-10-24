/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.trigger;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sonarlint.intellij.analysis.SonarLintJobManager;
import org.sonarlint.intellij.config.global.SonarLintGlobalSettings;
import org.sonarlint.intellij.util.SonarLintAppUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class FileEditorTriggerTest {
  @Mock
  private Project project;
  @Mock
  private SonarLintJobManager jobManager;
  @Mock
  private SonarLintAppUtils utils;

  private SonarLintGlobalSettings globalSettings;
  private FileEditorTrigger editorTrigger;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    globalSettings = new SonarLintGlobalSettings();
    MessageBus bus = mock(MessageBus.class);
    when(bus.connect(project)).thenReturn(mock(MessageBusConnection.class));
    when(project.getMessageBus()).thenReturn(bus);
    editorTrigger = new FileEditorTrigger(project, jobManager, globalSettings, utils);
  }

  @Test
  public void should_trigger() {
    globalSettings.setAutoTrigger(true);
    VirtualFile f1 = mock(VirtualFile.class);
    Module m1 = mock(Module.class);
    when(utils.findModuleForFile(f1, project)).thenReturn(m1);
    when(utils.shouldAnalyzeAutomatically(f1, m1)).thenReturn(true);

    editorTrigger.fileOpened(mock(FileEditorManager.class), f1);
    verify(jobManager).submitAsync(m1, Collections.singleton(f1), TriggerType.EDITOR_OPEN);
  }

  @Test
  public void should_not_trigger_if_fail_checks() {
    globalSettings.setAutoTrigger(true);
    VirtualFile f1 = mock(VirtualFile.class);
    Module m1 = mock(Module.class);
    when(utils.findModuleForFile(f1, project)).thenReturn(m1);
    when(utils.shouldAnalyzeAutomatically(f1, m1)).thenReturn(false);

    editorTrigger.fileOpened(mock(FileEditorManager.class), f1);
    verifyZeroInteractions(jobManager);
  }

  @Test
  public void should_not_trigger_if_disabled() {
    globalSettings.setAutoTrigger(false);
    verifyZeroInteractions(jobManager);
  }
}