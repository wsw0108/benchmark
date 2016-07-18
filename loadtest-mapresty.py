import random
from json import JSONEncoder, JSONDecoder

from locust import HttpLocust, TaskSet, task

DATABASE = "sdb"
LAYER_ID = "country_point"
TOTAL_COUNT = 3000000
PAGE_SIZE = 200
TOTAL_PAGE = TOTAL_COUNT/PAGE_SIZE


jsonEncoder = JSONEncoder()
jsonDecoder = JSONDecoder()


def check_response(response):
    if response.content is None:
        response.failure("empty content")
    elif response.content.rfind('"success":false') > 0:
        r = jsonDecoder.decode(response.content)
        response.failure(r.get("error") or "success: false")
    else:
        response.success()


class Query(TaskSet):
    @task
    def query_by_condition(self):
        page = random.choice(xrange(TOTAL_PAGE))
        data = "page=" + str(page) + "&" + "count=" + str(PAGE_SIZE)
        with self.client.post(
                "/rest/sdb/databases/%(db)s/layers/%(layerid)s/data?op=query" %
                {"db": DATABASE, "layerid": LAYER_ID},
                data, catch_response=True,
                name="query by condition") as response:
            check_response(response)

    @task
    def query_by_spatial(self):
        spatial_filter = {
            "relation": 0,
            "geometry": {
                "type": "Polygon",
                "coordinates": [[
                    [116.432, 30.386],
                    [116.432, 30.595],
                    [116.594, 30.595],
                    [116.594, 30.386]
                ]]
            }
        }
        crs = "WGS84"
        data = "page=0&count=1000"
        data += "&" + "spatialFilter=" + jsonEncoder.encode(spatial_filter) + "&" + "resultCRS=" + crs
        with self.client.post(
                "/rest/sdb/databases/%(db)s/layers/%(layerid)s/data?op=query" %
                {"db": DATABASE, "layerid": LAYER_ID},
                data, catch_response=True,
                name="query by spatial") as response:
            check_response(response)


class QueryUser(HttpLocust):
    task_set = Query
    host = "http://maptalks-server:11215"
