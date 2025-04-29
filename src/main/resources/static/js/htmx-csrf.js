document.addEventListener('DOMContentLoaded', function() {
    // Add the CSRF token to all HTMX requests
    document.body.addEventListener('htmx:configRequest', function(evt) {
        if (evt.detail.verb === "post" || evt.detail.verb === "put" || evt.detail.verb === "delete") {
            // Find the token in the page
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            // Add it to the request headers
            evt.detail.headers[csrfHeader] = csrfToken;
        }
    });
});