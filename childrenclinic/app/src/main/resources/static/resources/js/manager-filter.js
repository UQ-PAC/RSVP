/*
 * Copyright 2026 Gabriel Henrique Lopes Gomes Alves Nunes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function() {
    var levelSelect = document.querySelector('select[name="levelId"]');
    var managerSelect = document.getElementById('managerSelect');

    if (!levelSelect || !managerSelect) {
        return;
    }

    var hierarchy = [];
    var raw = levelSelect.getAttribute('data-level-hierarchy');
    if (raw) {
        try {
            hierarchy = JSON.parse(raw);
        } catch (e) {
            return;
        }
    }

    var nonManagers = [];
    var rawNon = levelSelect.getAttribute('data-non-manager-levels');
    if (rawNon) {
        try {
            nonManagers = JSON.parse(rawNon);
        } catch (e) {
            nonManagers = [];
        }
    }

    var allOptions = Array.from(managerSelect.options).map(function(opt) {
        return {
            value: opt.value,
            text: opt.text,
            level: opt.getAttribute('data-level'),
            selected: opt.selected
        };
    });

    function getValidManagerLevels(selectedLevel) {
        var idx = hierarchy.indexOf(selectedLevel);
        if (idx === -1) {
            return null;
        }
        var valid = [];
        for (var i = idx + 1; i < hierarchy.length; i++) {
            if (nonManagers.indexOf(hierarchy[i]) === -1) {
                valid.push(hierarchy[i]);
            }
        }
        return valid;
    }

    function filterManagers() {
        var selectedLevel = levelSelect.options[levelSelect.selectedIndex].text;
        var allowed = getValidManagerLevels(selectedLevel);
        var currentValue = managerSelect.value;

        managerSelect.innerHTML = '';

        var noneOpt = document.createElement('option');
        noneOpt.value = '';
        noneOpt.text = 'None';
        managerSelect.appendChild(noneOpt);

        for (var i = 0; i < allOptions.length; i++) {
            var opt = allOptions[i];
            if (opt.value === '') {
                continue;
            }
            if (!allowed || allowed.indexOf(opt.level) !== -1) {
                var o = document.createElement('option');
                o.value = opt.value;
                o.text = opt.text;
                o.setAttribute('data-level', opt.level || '');
                if (opt.value === currentValue) {
                    o.selected = true;
                }
                managerSelect.appendChild(o);
            }
        }
    }

    levelSelect.addEventListener('change', filterManagers);
    filterManagers();
})();
