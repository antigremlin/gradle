/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins;

import org.gradle.api.*;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.jvm.ClassDirectoryBinarySpecInternal;
import org.gradle.api.jvm.ClassDirectoryBinarySpec;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.java.JavaSourceSet;
import org.gradle.runtime.base.BinaryContainer;
import org.gradle.runtime.base.internal.BinaryNamingScheme;

import java.util.concurrent.Callable;

/**
 * Plugin for compiling Java code. Applies the {@link JvmLanguagePlugin}.
 * Adds a {@link JavaCompile} task for each {@link JavaSourceSet} added to a {@link org.gradle.api.jvm.ClassDirectoryBinarySpec}.
 * Registers the {@link JavaSourceSet} element type for each {@link org.gradle.language.base.FunctionalSourceSet} added to {@link org.gradle.language.base.ProjectSourceSet}.
 */
@Incubating
public class JavaLanguagePlugin implements Plugin<Project> {

    public void apply(final Project target) {
        target.getPlugins().apply(JvmLanguagePlugin.class);

        BinaryContainer jvmBinaryContainer = target.getExtensions().getByType(BinaryContainer.class);
        jvmBinaryContainer.withType(ClassDirectoryBinarySpecInternal.class).all(new Action<ClassDirectoryBinarySpecInternal>() {
            public void execute(final ClassDirectoryBinarySpecInternal binary) {
                final BinaryNamingScheme namingScheme = binary.getNamingScheme();
                binary.getSource().withType(JavaSourceSet.class).all(new Action<JavaSourceSet>() {
                    public void execute(JavaSourceSet javaSourceSet) {
                        // TODO: handle case where binary has multiple JavaSourceSet's
                        JavaCompile compileTask = target.getTasks().create(namingScheme.getTaskName("compile", "java"), JavaCompile.class);
                        configureCompileTask(compileTask, javaSourceSet, binary);
                        binary.getTasks().add(compileTask);
                        binary.builtBy(compileTask);
                    }
                });
            }
        });
    }


    /**
     * Preconfigures the specified compile task based on the specified source set and class directory binary.
     *
     * @param compile the compile task to be preconfigured
     * @param sourceSet the source set for the compile task
     * @param binary the binary for the compile task
     */
    public void configureCompileTask(AbstractCompile compile, final JavaSourceSet sourceSet, final ClassDirectoryBinarySpec binary) {
        compile.setDescription(String.format("Compiles %s.", sourceSet));
        compile.setSource(sourceSet.getSource());
        compile.dependsOn(sourceSet);
        ConventionMapping conventionMapping = compile.getConventionMapping();
        conventionMapping.map("classpath", new Callable<Object>() {
            public Object call() throws Exception {
                return sourceSet.getCompileClasspath().getFiles();
            }
        });
        conventionMapping.map("destinationDir", new Callable<Object>() {
            public Object call() throws Exception {
                return binary.getClassesDir();
            }
        });
    }
}
