'use strict';
(function () {
    function escapeHtml(str) {
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function init() {
        var form = document.getElementById('intake-form');
        if (!form) return;

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            var decisionContainer = document.getElementById('decision-container');
            decisionContainer.innerHTML = '<p class="text-muted">Analizuję zgłoszenie…</p>';

            var formData = new FormData(form);
            fetch('/submit', {
                method: 'POST',
                body: formData
            }).then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (body) {
                        if (body.includes('<p class="error-message">')) {
                            decisionContainer.innerHTML = body;
                        } else {
                            decisionContainer.innerHTML = '<p class="error-message">Wystąpił błąd. Spróbuj ponownie.</p>';
                        }
                    });
                }
                decisionContainer.innerHTML = '';
                var reader = response.body.getReader();
                var decoder = new TextDecoder();
                function read() {
                    reader.read().then(function (result) {
                        if (result.done) return;
                        var chunk = decoder.decode(result.value, { stream: true });
                        chunk.split('\n').forEach(function (line) {
                            if (line.startsWith('data:')) {
                                var text = line.slice(5).trim();
                                if (!text) return;
                                if (text.startsWith('ERROR:')) {
                                    var msg = text.slice(6).trim();
                                    decisionContainer.innerHTML = '<p class="error-message">' + escapeHtml(msg) + '</p>';
                                    return;
                                }
                                decisionContainer.innerHTML += text;
                            }
                        });
                        read();
                    });
                }
                read();
            }).catch(function () {
                decisionContainer.innerHTML = '<p class="error-message">Nie można połączyć się z serwerem. Sprawdź połączenie i spróbuj ponownie.</p>';
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
