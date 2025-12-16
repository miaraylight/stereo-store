function apiUrl(path) {
    return `${config.apiBase}${path.startsWith("/") ? path : "/" + path}`;
}