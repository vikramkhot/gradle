/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.play.internal.twirl;

import com.google.common.collect.Lists;
import org.gradle.api.tasks.WorkResult;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.scala.internal.reflect.ScalaMethod;
import org.gradle.scala.internal.reflect.ScalaOptionInvocationWrapper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Twirl compiler uses reflection to load and invoke the actual compiler classes/methods.
 * See spec.versions for individual methods
 */
public class TwirlCompiler implements Compiler<VersionedTwirlCompileSpec>, Serializable {

    public WorkResult execute(VersionedTwirlCompileSpec spec) {
        ArrayList<File> outputFiles = Lists.newArrayList();
        try {
            ClassLoader cl = getClass().getClassLoader();
            ScalaMethod compile = spec.getCompileMethod(cl);
            Iterable<File> sources = spec.getSources();
            for (File sourceFile : sources) {
                Object result = compile.invoke(spec.createCompileParameters(cl, sourceFile));
                ScalaOptionInvocationWrapper<File> maybeFile = new ScalaOptionInvocationWrapper<File>(result);
                if (maybeFile.isDefined()) {
                    outputFiles.add(maybeFile.get());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error invoking Play Twirl template compiler.", e);
        }

        return new TwirlCompilerWorkResult(outputFiles);
    }


}
