import logging
from datetime import datetime
import pytz

EPOCH = datetime(1970, 1, 1, tzinfo=pytz.utc)


def _display_period_parser(obj):
    logger = logging.getLogger('display_period_parser')
    required_fields = ['start', 'end', 'time_zone']

    if not all([k in obj.keys() for k in required_fields]):
        logger.error('Missing required fields [%s] in [%s]', required_fields, obj)
        return False, None

    time_zone = obj['time_zone']
    if not time_zone in pytz.all_timezones:
        logger.error('Unknown timezone: [%s].', time_zone)
        return False, None

    start_time = convert_to_timestamp(obj['start'], time_zone)
    if start_time is None:
        logger.error('Failed to parse time: [%s %s]', obj['start'], time_zone)
        return False, None

    end_time = convert_to_timestamp(obj['end'], time_zone)
    if end_time is None:
        logging.error('Failed to parse end time: [%s %s]', obj['end'])
        return False, None

    return True, {
        'start_time': start_time,
        'end_time': end_time
    }


def convert_to_timestamp(time_str, time_zone):
    """
    Converts the given string representation to number of seconds since the Epoch
    :param time_str:
    :param time_zone:
    :return: number of seconds since the EPOCH
    """
    try:
        dt = datetime.strptime(time_str, '%Y%m%d:%H:%M:%S')
        tz = pytz.timezone(time_zone)
        dt_with_tz = tz.localize(dt)
        ts = int((dt_with_tz - EPOCH).total_seconds())
        return ts
    except ValueError:
        return None


AD_FILE_FORMAT = {
    'version': int,
    'pub_timestamp': int,
    'ad_content': [
        {
            'id': int,
            'content': {
                'img_url': unicode
            },
            'display_period': _display_period_parser
        }
    ]
}


if __name__ == '__main__':
    from_jst = convert_to_timestamp("20180520:18:00:00", "Japan")
    from_utc = convert_to_timestamp("20180520:09:00:00", "UTC")
    print from_jst
    assert from_jst == from_utc
