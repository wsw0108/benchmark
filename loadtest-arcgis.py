import random
from json import JSONDecoder

from locust import HttpLocust, TaskSet, task


jsonDecoder = JSONDecoder()


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
        samples = random.sample(xrange(1, 3000000), 200)
        where = "objectid in (" + ",".join(str(v) for v in samples) + ")"
        data = {
            "where": where,
            "outFields": "*",
            "f": "json"
        }
        with self.client.post(
                "/arcgis/rest/services/ChinaCountry/FeatureServer/0/query",
                data, catch_response=True,
                name="query by condition") as response:
            check_response(response)

    @task
    def query_by_spatial(self):
        data = {
            "geometry": "116.432,30.386,116.594,30.595",
            "geometryType": "esriGeometryEnvelope",
            "spatialRel": "esriSpatialRelIntersects",
            "outFields": "*",
            "f": "json"
        }
        with self.client.post(
                "/arcgis/rest/services/ChinaCountry/FeatureServer/0/query",
                data, catch_response=True,
                name="query by spatial") as response:
            check_response(response)


class QueryUser(HttpLocust):
    task_set = Query
    host = "http://localhost:6080"
