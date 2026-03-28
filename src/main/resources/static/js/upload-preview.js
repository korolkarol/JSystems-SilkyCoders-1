'use strict';

(function () {
    var MAX_SIZE = 10 * 1024 * 1024;
    var ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

    function init() {
        var fileInput = document.getElementById('photo');
        var submitBtn = document.getElementById('submit-btn');
        var errorEl = document.getElementById('file-error');
        var previewEl = document.getElementById('image-preview');

        if (!fileInput || !submitBtn) {
            return;
        }

        submitBtn.disabled = true;

        fileInput.addEventListener('change', function () {
            clearError();
            clearPreview();
            submitBtn.disabled = true;

            var file = fileInput.files && fileInput.files[0];
            if (!file) {
                return;
            }

            if (ALLOWED_TYPES.indexOf(file.type) === -1) {
                showError('Nieobsługiwany format pliku. Dozwolone formaty: JPG, PNG, WEBP.');
                fileInput.value = '';
                return;
            }

            if (file.size > MAX_SIZE) {
                showError('Plik jest zbyt duży. Maksymalny rozmiar to 10 MB.');
                fileInput.value = '';
                return;
            }

            showPreview(file);
            submitBtn.disabled = false;
        });

        function showError(message) {
            if (errorEl) {
                errorEl.textContent = message;
                errorEl.style.color = '#FF0023';
                errorEl.style.fontSize = '14px';
                errorEl.style.marginTop = '8px';
            }
        }

        function clearError() {
            if (errorEl) {
                errorEl.textContent = '';
            }
        }

        function showPreview(file) {
            if (!previewEl) {
                return;
            }
            var reader = new FileReader();
            reader.onload = function (e) {
                var img = document.createElement('img');
                img.src = e.target.result;
                img.style.maxWidth = '100%';
                img.style.maxHeight = '200px';
                img.style.marginTop = '8px';
                img.alt = 'Podgląd zdjęcia';
                previewEl.appendChild(img);
            };
            reader.readAsDataURL(file);
        }

        function clearPreview() {
            if (previewEl) {
                previewEl.innerHTML = '';
            }
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    document.addEventListener('htmx:load', init);
})();
