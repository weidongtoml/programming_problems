import hashlib
import logging
import pprint
from ad_format import AD_FILE_FORMAT
import generic_json_loader


class AdContentUpdater:
    """
    This is responsible for loading ad content from a local file
    """
    AD_FILE_NAME = "banner_ads.json"
    CHECKSUM_FILE_NAME = "banner_ads.md5"

    def __init__(self, ad_dir):
        self._ad_dir = ad_dir
        self._ad_contents = None
        self._logger = logging.getLogger('AdContentUpdater')

    def load(self):
        self._logger.info('Loading ad content from path [%s]' % self._ad_dir)
        # load md5 checksum
        checksum_path = self._ad_dir + '/' + self.CHECKSUM_FILE_NAME
        checksum_file = open(checksum_path, 'r')
        if checksum_file is None:
            self._logger.error('Checksum file [%s] does not exist.' % checksum_path)
            return False
        checksum = checksum_file.readline().strip()
        if len(checksum) != 32:
            self._logger.error('Invalid checksum [%s], expected 32 bytes.' % checksum)
            return False
        # load ad contents
        adcontent_path = self._ad_dir + '/' + self.AD_FILE_NAME
        adcontent_file = open(adcontent_path, 'r')
        if adcontent_file is None:
            self._logger.error('Cannot open ad content file [%s].' % adcontent_path)
            return False
        all_ad_contents = ''.join(adcontent_file.readlines())
        file_checksum = self.do_checksum(all_ad_contents)
        if file_checksum != checksum:
            self._logger.error('Expected checksum to be [%s] but got [%s].' % (checksum, file_checksum))
            return False
        # parse add content
        (parsed_ok, parsed_content) = generic_json_loader.load_json(AD_FILE_FORMAT, all_ad_contents)
        if not parsed_ok:
            self._logger.error('Failed to load ad content')
            return False

        self._logger.debug('Loaded add content: \n%s\n.', pprint.pprint(parsed_content))
        self._logger.info('Finished loading ad content.')
        self._ad_contents = parsed_content
        return True

    @staticmethod
    def do_checksum(text_content):
        m = hashlib.md5()
        m.update(text_content)
        return m.hexdigest()

    def get_ad_contents(self):
        return self._ad_contents


