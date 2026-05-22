(function() {
    var levelSelect = document.querySelector('select[name="levelId"]');
    var specialtiesSelect = document.querySelector('select[name="specialties"]');

    if (!levelSelect || !specialtiesSelect) {
        return;
    }

    var specialtiesContainer = specialtiesSelect.closest('.control-group');
    if (!specialtiesContainer) {
        return;
    }

    function filterSpecialties() {
        var selectedLevel = levelSelect.options[levelSelect.selectedIndex].text;
        if (selectedLevel === 'Specialist') {
            specialtiesContainer.style.display = '';
        } else {
            specialtiesContainer.style.display = 'none';
            for (var i = 0; i < specialtiesSelect.options.length; i++) {
                specialtiesSelect.options[i].selected = false;
            }
        }
    }

    levelSelect.addEventListener('change', filterSpecialties);
    filterSpecialties();
})();
