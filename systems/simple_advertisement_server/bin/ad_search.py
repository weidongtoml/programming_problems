import json
import pprint

from ad_content_updater import AdContentUpdater
from ad_format import convert_to_timestamp


class AdSearcher:
    def __init__(self, ad_contents):
        self._ad_contents = ad_contents
        self._ad_doc_store = {}
        self._period_index = []  # sorted by the end_time
        for ad in ad_contents['ad_content']:
            period = ad['display_period']
            self._ad_doc_store[ad['id']] = ad
            self._period_index.append((period['start_time'], period['end_time'], ad['id']))
        self._period_index = sorted(self._period_index, key=lambda x: x[1])

    def search_ad_ids(self, query):
        cur_time = query['current_time']
        is_return_all_active = query['all_active'] if 'all_active' in query else False

        # find all ads that has the ending date greater than cur_time (i.e. not outdated, then filter
        # out those that have not yet started if needed.
        i = self._get_active_ads_start_index(cur_time)
        if i == len(self._period_index):
            return []

        if is_return_all_active:
            return [p[2] for p in self._period_index[i:]]
        else:
            return [p[2] for p in self._period_index[i:] if p[0] <= cur_time]

    def get_ad_by_ids(self, ids):
        return [self._ad_doc_store[id] for id in ids]

    def get_status(self):
        return json.dumps(self._ad_contents, indent=4)

    def _get_active_ads_start_index(self, cur_time):
        lo = 0
        hi = len(self._period_index)
        while lo < hi:
            mid = (lo + hi) // 2
            if self._period_index[mid][1] < cur_time:
                lo = mid + 1
            else:
                hi = mid
        return lo


def test_AdSearcher():
    ad_contents = {
        'ad_content': [
            {
                'id': 1,
                'display_period': {
                    'start_time': 10,
                    'end_time': 10
                }
            }, {
                'id': 2,
                'display_period': {
                    'start_time': 10,
                    'end_time': 20
                }
            }, {
                'id': 3,
                'display_period': {
                    'start_time': 40,
                    'end_time': 100
                }
            }, {
                'id': 4,
                'display_period': {
                    'start_time': 50,
                    'end_time': 90
                }
            }, {
                'id': 5,
                'display_period': {
                    'start_time': 1,
                    'end_time': 5
                }
            }, {
                'id': 6,
                'display_period': {
                    'start_time': 5,
                    'end_time': 9
                }
            }
        ]
    }
    ad_searcher = AdSearcher(ad_contents)
    res1 = sorted(ad_searcher.search_ad_ids({
        'current_time': 10
    }))
    assert [1, 2] == res1

    res2 = ad_searcher.search_ad_ids({
        'current_time': 20
    })
    assert [2] == res2

    res3 = sorted(ad_searcher.search_ad_ids({
        'current_time': 60
    }))
    assert [3, 4] == res3

    res4 = sorted(ad_searcher.search_ad_ids({
        'current_time': 2
    }))
    assert [5] == res4

    res5 = sorted(ad_searcher.search_ad_ids({
        'current_time': 5
    }))
    assert [5, 6] == res5

    res6 = sorted(ad_searcher.search_ad_ids({
        'current_time': 2,
        'all_active': True
    }))
    assert [1, 2, 3, 4, 5, 6] == res6

    res7 = sorted(ad_searcher.search_ad_ids({
        'current_time': 10,
        'all_active': True
    }))
    assert [1, 2, 3, 4] == res7


if __name__ == '__main__':
    test_AdSearcher()

