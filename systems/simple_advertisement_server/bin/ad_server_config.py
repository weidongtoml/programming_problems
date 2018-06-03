import logging
import pprint
import socket
import os

import generic_json_loader


def _is_valid_ip(value):
    try:
        socket.inet_aton(value)
        return True, value
    except socket.error:
        return False, value


_SERVER_CONF_FORMAT = {
    'server_host': unicode,
    'server_port': int,
    'privileged_ips': [
        _is_valid_ip
    ]
}


class AdServerConfig:
    def __init__(self, config_dir="../conf"):
        self._config_file = config_dir + '/ad_server_conf.json'
        self._logger = logging.getLogger('AdServerConfig')

    def load(self):
        self._logger.info('Loading config with working directory: [%s].' % os.getcwd())
        try:
            conf_file = open(self._config_file, 'r')
            content = ''.join(conf_file.readlines())
            is_ok, conf_obj = generic_json_loader.load_json(_SERVER_CONF_FORMAT, content)
            if is_ok:
                self._logger.info('Finished loading configuration from [%s].', self._config_file)
                return conf_obj
            else:
                self._logger.error('Failed while parsing configuration file [%s].', self._config_file)
                return None
        except IOError as e:
            self._logger.error('Failed to open configuration file [%s]: %s.' % (self._config_file, e))
            return None


if __name__ == '__main__':
    logging.basicConfig(
        filename='../logs/test_ad_server_config.log',
        level=logging.DEBUG)
    server_conf = AdServerConfig()
    pprint.pprint(server_conf.load())

