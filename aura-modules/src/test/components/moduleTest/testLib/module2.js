/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function module2() { //eslint-disable-line no-unused-vars

    var lib = { //eslint-disable-line no-shadow, no-unused-vars
        exportMethod1: function () { console.log('module2:method1'); },
        exportMethod2: function () { console.log('module2:method2'); },
        exportMethod3: function () { console.log('module2:method3'); }
    };

    return lib;
}
