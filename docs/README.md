# Python Project Template
> [!Caution]
> Codeblocks are reversed due to a pycharm bug, reverse them if you want to use them in VSCode

This template provides a basic structure for python projects.

It uses:
- [Poetry](https://python-poetry.org/) as package manager
- [Sphinx](https://www.sphinx-doc.org/en/master/) for automatic documentation
- [Pytest](https://docs.pytest.org/en/stable/) for testing
- [Pre-commit](https://pre-commit.com/) as CI pipeline for code formatting and linting

> [!NOTE]
> Use this template via `git clone https://github.com/oliverstoll/python-template` to avoid the *"generated by"* note on GitHub

# How to install the Project

### Install dependencies & pre-commit hooks 
```bash
pre-commit install
poetry install --no-root
pip install poetry
.\.venv\Scripts\activate
```

This by default installs only main and dev dependencies. If you want to install cloud dependencies as well, use the following command:

```bash
poetry install --with cloud
```

### Hide unnecessary files (*PyCharm*)
*Settings -> Editor -> File Types -> Ignore files and folders*
- poetry.lock
- .mypy_cache
- .pytest_cache
- .ruff_cache

### Create new Readme
When set up, you should create a new Readme File that describes the project and its usage.


# How to use the Project


### Run Unit-Tests
```bash
pytest
```

### Serve Sphinx Documentation
```bash
# check that conf.py has the right path: sys.path.insert(0, os.path.abspath(".."))
sphinx-serve -h localhost
make html
sphinx-apidoc -t _templates -o source/ ../../src
make clean
cd docs/sphinx
```


# How to Deploy to Cloud
- Replace the `[PROJECT-ID]`, `[REPO]` and `[IMAGE]` with your gcloud project, gcr repository name and the image name you want
- check you installed all dependencies using `poetry add` and not `pip install`
```bash
# build docker image
docker build -t europe-west1-docker.pkg.dev/[PROJECT-ID]/[REPO]/[IMAGE] .
```

```bash
# test locally
docker run -p 8080:8080 europe-west1-docker.pkg.dev/[PROJECT-ID]/[REPO]/[IMAGE]
```

```bash
# push to artifact registry
docker push europe-west1-docker.pkg.dev/[PROJECT-ID]/[REPO]/[IMAGE]
```

### Now you can deploy using gcloud sdk:
```bash
# we use image as service name and allow unauthenticated access
gcloud run deploy [IMAGE]  --allow-unauthenticated
--image=europe-west1-docker.pkg.dev/[PROJECT-ID]/[REPO]/[IMAGE]:latest --region=europe-west1 --project=[PROJECT-ID]
```
