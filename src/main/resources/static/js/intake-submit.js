'use strict';
(function () {
    function init() {
        var form = document.getElementById('intake-form');
        if (!form) return;

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            var decisionContainer = document.getElementById('decision-container');
            decisionContainer.innerHTML = '<p>Analizuję zgłoszenie...</p>';

            var formData = new FormData(form);
            fetch('/submit', {
                method: 'POST',
                body: formData
            }).then(function (response) {
                if (!response.ok) {
                    return response.text().then(function (text) {
                        decisionContainer.innerHTML = '<p style="color:#FF0023">' + text + '</p>';
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
                                if (text) {
                                    decisionContainer.innerHTML += text;
                                }
                            }
                        });
                        read();
                    });
                }
                read();
            }).catch(function () {
                decisionContainer.innerHTML = '<p style="color:#FF0023">Błąd połączenia.</p>';
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
