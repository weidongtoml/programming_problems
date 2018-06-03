import json
import logging

_logger = logging.getLogger('generic_json_loader')


def load_json(file_format, json_file_content):
    json_content = json.loads(json_file_content)
    is_ok = _check_and_parse_ad_format(file_format, json_content)
    return is_ok, json_content


def _is_basic_type(format_type):
    return format_type in [int, str, unicode]


def _check_basic_type(field_name, field_type, field_value):
    if not isinstance(field_value, field_type):
        _logger.error('Expected to get type [%s] but got [%s] for [%s].' % (
            field_type, field_value, field_name
        ))
        return False
    else:
        return True


def _is_function_type(field_type):
    return (not _is_basic_type(field_type)) and callable(field_type)


def _transform(field_name, field_type, field_value):
    # TODO(weidong): update field_type function to return a tuple with first to denote correctness
    is_ok, parsed_value = field_type(field_value)
    if not is_ok:
        _logger.error('Failed to parse field [%s] with value [%s].', field_name, field_value)
    return is_ok, parsed_value


def _check_and_parse_ad_format(ad_format, content, field_name='global'):
    if _is_basic_type(ad_format):
        return _check_basic_type(field_name, ad_format, content)

    is_format_correct = True
    for (field_name, field_type) in ad_format.items():
        # check that the required field exists
        if field_name not in content.keys():
            _logger.error('Missing required field [%s].', field_name)
            is_format_correct = False
            continue

        field_value = content[field_name]
        if _is_function_type(field_type):
            is_ok, content[field_name] = _transform(field_name, field_type, field_value)
            is_format_correct = is_format_correct and is_ok
        elif _is_basic_type(field_type):
            is_format_correct = is_format_correct and _check_basic_type(field_name, field_type, field_value)
        elif isinstance(field_type, list):
            # the required field is a list, check each element matches the type
            if len(field_type) != 1:
                raise 'AD_FILE_FORMAT error, expected [%s] to have length of 1.' % field_type
            if not isinstance(field_value, list):
                _logger.error('Expected field_type of [%s] to be list but got [%s]', field_name, field_value)
                is_format_correct = False
            else:
                child_format = field_type[0]
                children_correct = True
                if _is_basic_type(child_format):
                    children_correct = all([_check_basic_type(field_name, child_format, c) for c in field_value])
                elif _is_function_type(child_format):
                    results = [_transform(field_name, child_format, c) for c in field_value]
                    children_correct = all(r[0] for r in results)
                    content[field_name] = [r[1] for r in results]
                else:
                    children_correct = all([_check_and_parse_ad_format(child_format, c) for c in field_value])
                if not children_correct:
                    _logger.error('Failed while parsing members for field [%s] of type [%s].', field_name, field_type)
                is_format_correct = is_format_correct and children_correct
        elif isinstance(field_type, dict):
            # the required field is a dict, check it against each fields
            if not isinstance(field_value, dict):
                _logger.error('Expected field_type of [%s] to be dict but got [%s]', field_name, field_value)
                is_format_correct = False
            else:
                is_format_correct = is_format_correct and _check_and_parse_ad_format(field_type, field_value)

    return is_format_correct
