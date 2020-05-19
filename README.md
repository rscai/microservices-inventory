# Microservices - Inventory service

## Metrics

Item|Status
--|------------
CI|[![Build Status](https://travis-ci.org/rscai/microservices-inventory.svg?branch=master)](https://travis-ci.org/rscai/microservices-inventory)
Code Quality|[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=rscai_microservices-inventory&metric=alert_status)](https://sonarcloud.io/dashboard?id=rscai_microservices-inventory)
Coverage|[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=rscai_microservices-inventory&metric=coverage)](https://sonarcloud.io/dashboard?id=rscai_microservices-inventory)
Line of Code|[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=rscai_microservices-inventory&metric=ncloc)](https://sonarcloud.io/dashboard?id=rscai_microservices-inventory)
Technical Debt|[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=rscai_microservices-inventory&metric=sqale_index)](https://sonarcloud.io/dashboard?id=rscai_microservices-inventory)

## Development

### Setup MySQL
   
   Create and run MySQL instance via Docker:
   
   ```shell
   docker run --name inventory-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=inventory -e MYSQL_USER=test -e MYSQL_PASSWORD=passwd -d mysql:5.7
   ```
   
   Connect instance by client tool:
   
   ```shell
   docker exec -it inventory-mysql bash
   ```