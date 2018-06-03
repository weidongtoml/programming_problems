import BaseHTTPServer
import json
import logging
import urlparse
import time

from ad_content_updater import AdContentUpdater
from ad_format import convert_to_timestamp
from ad_search import AdSearcher
from ad_server_config import AdServerConfig


class AdServer(BaseHTTPServer.HTTPServer):
    """
    AdServer: a standalone HTTP server that can be used to serve ads.
    """
    class AdServerRequestHandler(BaseHTTPServer.BaseHTTPRequestHandler):
        def do_GET(self):
            self.server.handle_get(self)

    def __init__(self):
        self._setup_logging()
        self._logger = logging.getLogger('AdServer')
        self._ad_search = None
        self._config = self._load_config()
        if self._config is None:
            raise RuntimeError('Failed to load server configuration')
        server_address = (self._config['server_host'], self._config['server_port'])
        BaseHTTPServer.HTTPServer.__init__(self, server_address, self.AdServerRequestHandler)

    def setup(self):
        if not self._load_ads():
            return False
        return True

    def run(self):
        # server states sanity check
        if self._ad_search is None:
            raise RuntimeError("Missing ad contents")

        while True:
            self.handle_request()

    def _setup_logging(self):
        FORMAT = "%(asctime)-15s %(message)s"
        # TODO(weidong): change logging level depending on whether this is production or development
        logging.basicConfig(
            filename='./logs/adserver.log',
            level=logging.DEBUG,
            format=FORMAT)

    def _load_ads(self):
        ad_content_updater = AdContentUpdater('./ads')
        if not ad_content_updater.load():
            self._logger.error('Failed to load ad content')
            return False
        ad_contents = ad_content_updater.get_ad_contents()
        if ad_contents is None:
            raise RuntimeError('Expected ad_contents to exist but got None.')
        self._ad_search = AdSearcher(ad_content_updater.get_ad_contents())
        return True

    def _load_config(self):
        config_loader = AdServerConfig('./conf/')
        return config_loader.load()

    def handle_get(self, request_handler):
        handlers = {
            '/api/get_ad': self._get_ad,
            '/internal/status': self._get_status,
            '/internal/states': self._get_states
        }
        try:
            parsed_url = urlparse.urlparse(request_handler.path)
            print parsed_url
            handlers[parsed_url.path](request_handler)
        except KeyError as e:
            logging.error('Unknown API path [%s], %s' % (request_handler.path, e))
            self._handle_unknown_method_error(request_handler)

    def _handle_unknown_method_error(self, request_handler):
        request_handler.send_response(404)
        request_handler.send_header('Content-Type', 'text/html')
        request_handler.end_headers()
        request_handler.wfile.write('Unknown method: %s' % request_handler.path)

    def _get_ad(self, request_handler):
        search_param = self._parse_request_param(request_handler)
        ad_ids = self._ad_search.search_ad_ids(search_param)
        ads = self._ad_search.get_ad_by_ids(ad_ids)
        self._output_ads(request_handler, ads)

    def _parse_request_param(self, request_handler):
        parsed_url = urlparse.urlparse(request_handler.path)
        current_time = None
        is_all_active = False
        if len(parsed_url.query) > 0:
            try:
                params = dict([p.split('=', 1) for p in parsed_url.query.split('&')])
                current_time = convert_to_timestamp(params['dt'], params['tz']) \
                    if 'dt' in params and 'tz' in params else None
                is_all_active = int(params['all']) == 1 if 'all' in params else False
            except ValueError as e:
                self._logger.error('Error encountered when parsing parameter: %s' % e)
        # only privilege_client can override current time and ask to return all active ads
        is_privilege_client = self._is_priviledged_client(request_handler)
        current_time = current_time if (current_time is not None and is_privilege_client) \
            else int(round(time.time()))
        is_all_active = is_all_active and is_privilege_client
        return {
            'current_time': current_time,
            'all_active': is_all_active
        }

    def _output_ads(self, request_handler, ads):
        ads_to_return = [{'id': ad['id'], 'img': ad['content']['img_url']} for ad in ads]
        self._http_ok(request_handler)
        request_handler.wfile.write(json.dumps(ads_to_return, indent=4))

    def _get_status(self, request_handler):
        if self._is_priviledged_client(request_handler):
            self._http_ok(request_handler)
            status_obj = {
                'status': 'ok' if (self._ad_search is not None) else 'outage'
            }
            request_handler.wfile.write(json.dumps(status_obj))
        else:
            self._http_unauthorized_access(request_handler)

    def _get_states(self, request_handler):
        if self._is_priviledged_client(request_handler):
            self._http_ok(request_handler)
            request_handler.wfile.write(self._ad_search.get_status())
        else:
            self._http_unauthorized_access(request_handler)

    def _http_ok(self, request_handler):
        request_handler.send_response(200)
        request_handler.send_header('Content-Type', 'text/json')
        request_handler.end_headers()

    def _http_unauthorized_access(self, request_handler):
        request_handler.send_response(403)
        request_handler.send_header('Content-Type', 'text')
        request_handler.end_headers()

    def _is_priviledged_client(self, request_handler):
        client_ip = request_handler.client_address[0]
        return client_ip in self._config['privileged_ips']


if __name__ == '__main__':
    ad_server = AdServer()
    if ad_server.setup():
        ad_server.run()


