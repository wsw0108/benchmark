import random
import json

from locust import HttpLocust, TaskSet, task

NAME = "mapresty"
TOTAL = 3000000
LIMIT = 2000

DATABASE = "sdbm"
LAYER_ID = "country_point"


jsonEncoder = json.JSONEncoder()
jsonDecoder = json.JSONDecoder()


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
        samples = random.sample(xrange(1, TOTAL), LIMIT)
        where = "{ objectid: { $in: [" + ",".join(str(v)
                                                  for v in samples) + "] } }"
        data = {"page": 0, "count": LIMIT, "condition": where}
        with self.client.post(
                "/rest/sdb/databases/%(db)s/layers/%(layerid)s/data?op=query" %
                {"db": DATABASE, "layerid": LAYER_ID},
                data,
                catch_response=True,
                name="(%s) query by condition" % NAME) as response:
            check_response(response)

    @task
    def query_by_spatial(self):
        spatial_filter = {
            "relation": 0,
            "geometry": {
                "type": "Polygon",
                "coordinates": [[
                    [110.588, 32.575],
                    [112.492, 32.118],
                    [113.862, 31.166],
                    [113.234, 29.929],
                    [111.445, 30.290],
                    [110.170, 28.235]
                ]]
            }
        }
        data = {
            "page": 0,
            "count": LIMIT,
            "resultCRS": "WGS84",
            "spatialFilter": jsonEncoder.encode(spatial_filter)
        }
        with self.client.post(
                "/rest/sdb/databases/%(db)s/layers/%(layerid)s/data?op=query" %
                {"db": DATABASE, "layerid": LAYER_ID},
                data,
                catch_response=True,
                name="(%s) query by spatial" % NAME) as response:
            check_response(response)


class QueryUser(HttpLocust):
    task_set = Query
    host = "http://maptalks-server:11215"
