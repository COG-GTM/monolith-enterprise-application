"""Hexagonal architecture boundary tests."""

import ast
from pathlib import Path


def test_domain_does_not_import_infrastructure() -> None:
    domain_path = Path(__file__).parents[1] / "snowman" / "domain"
    for module_path in domain_path.rglob("*.py"):
        tree = ast.parse(module_path.read_text(), filename=str(module_path))
        for node in ast.walk(tree):
            if isinstance(node, ast.Import):
                imported_names = [alias.name for alias in node.names]
            elif isinstance(node, ast.ImportFrom):
                imported_names = [node.module or ""]
            else:
                continue
            assert all(
                not imported.startswith("snowman.infrastructure")
                for imported in imported_names
            ), f"{module_path} imports infrastructure"
