import random
import json

from locust import HttpLocust, TaskSet, task

NAME = "arcgis"
TOTAL = 3000000
LIMIT = 2000


jsonDecoder = json.JSONDecoder()


def check_response(response):
    if response.content is None:
        response.failure("empty content")
    elif response.content.find('"error": {') > 0:
        r = jsonDecoder.decode(response.content)
        response.failure(r.get("error").get("message") or "error response")
    else:
        response.success()


class Query(TaskSet):

    @task
    def query_by_condition(self):
        samples = random.sample(xrange(1, TOTAL), LIMIT)
        where = "objectid in (" + ",".join(str(v) for v in samples) + ")"
        data = {
            "where": where,
            "outFields": "*",
            "f": "json"
        }
        with self.client.post(
                "/arcgis/rest/services/ChinaMap2012/FeatureServer/4/query",
                data,
                catch_response=True,
                name="(%s) query by condition" % NAME) as response:
            check_response(response)

    @task
    def query_by_spatial(self):
        geometry = {
            "rings": [[
                [110.588, 32.575],
                [112.492, 32.118],
                [113.862, 31.166],
                [113.234, 29.929],
                [111.445, 30.290],
                [110.170, 28.235]
            ]]
        }
        data = {
            "geometry": json.dumps(geometry),
            "geometryType": "esriGeometryPolygon",
            "spatialRel": "esriSpatialRelIntersects",
            "outFields": "*",
            "f": "json"
        }
        with self.client.post(
                "/arcgis/rest/services/ChinaMap2012/FeatureServer/4/query",
                data,
                catch_response=True,
                name="(%s) query by spatial" % NAME) as response:
            check_response(response)


class QueryUser(HttpLocust):
    task_set = Query
    host = "http://arcgis-server:6080"
