import json
import urllib2


def get_api_url(api_path):
    return "http://localhost:8000" + api_path


def get_api_result(api_path):
    return urllib2.urlopen(get_api_url(api_path))


def parse_json_result(response):
    return json.loads(''.join(response.readlines()))


def _test_api_get_1():
    """
    Test time period that matches the starting time
    """
    response = get_api_result("/api/get_ad?dt=20180101:00:00:00&tz=UTC")
    assert response.code == 200
    result = parse_json_result(response)
    expected = [{
        'id': 1,
        'img': 'img_url_1'
    }]
    assert result == expected


def _test_api_get_2():
    """
    Test time period that matches the ending time
    """
    response = get_api_result("/api/get_ad?dt=20180719:12:05:01&tz=UTC")
    assert response.code == 200
    result = parse_json_result(response)
    expected = [{
        'id': 2,
        'img': 'img_url_2'
    }, {
        'id': 3,
        'img': 'img_url_3'
    }]
    assert result == expected


def _test_api_get_3():
    """
    Test against advertisement time not in UTC
    """
    response = get_api_result("/api/get_ad?dt=19970102:09:00:00&tz=UTC")
    assert response.code == 200
    result = parse_json_result(response)
    expected = [{
        'id': 4,
        'img': 'img_url_4'
    }]
    assert result == expected


def _test_api_get_4():
    """
    Test against current time and advertisement time not in UTC
    """
    response = get_api_result("/api/get_ad?dt=19970102:19:00:00&tz=Japan")
    assert response.code == 200
    result = parse_json_result(response)
    expected = [{
        'id': 4,
        'img': 'img_url_4'
    }]
    assert result == expected


def _test_api_get_5():
    """
    Test getting all valid advertisements
    """
    response = get_api_result("/api/get_ad?dt=20180518:00:00:00&tz=UTC&all=1")
    assert response.code == 200
    result = parse_json_result(response)
    expected = [{
        'id': 2,
        'img': 'img_url_2'
    }, {
        'id': 3,
        'img': 'img_url_3'
    }]
    assert result == expected


def _test_api_get_6():
    """
    Test against empty result
    """
    response = get_api_result("/api/get_ad?dt=19960102:19:00:00&tz=Japan")
    assert response.code == 200
    result = parse_json_result(response)
    expected = []
    assert result == expected


def test_api_get_ad():
    _test_api_get_1()
    _test_api_get_2()
    _test_api_get_3()
    _test_api_get_4()
    _test_api_get_5()
    _test_api_get_6()


def test_internal_status():
    response = get_api_result('/internal/status')
    assert response.code == 200
    status = parse_json_result(response)
    assert "status" in status
    assert status["status"] == 'ok'


def test_internal_states():
    response = get_api_result('/internal/states')
    assert response.code == 200
    result = parse_json_result(response)
    assert result != {}


if __name__ == '__main__':
    test_cases = [
        test_api_get_ad,
        test_internal_states,
        test_internal_status
    ]

    for test_case in test_cases:
        test_case()

