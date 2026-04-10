# Despliegue "todo en AWS" (CloudFormation)

Esta versión de la plantilla crea toda la base de infraestructura dentro de AWS:

- VPC, subredes públicas y privadas, Internet Gateway y NAT Gateway
- Repositorio ECR
- Secrets Manager (JWT y credenciales de DB)
- Amazon DocumentDB (cluster + instancia)
- ECS Fargate + ALB + CloudWatch Logs + roles IAM

La única entrada externa sigue siendo tu imagen Docker (que se publica en ECR).

## Prerrequisitos

- AWS CLI configurado (`aws configure`)
- Docker instalado
- Permisos para CloudFormation, ECR, ECS, EC2, IAM, Secrets Manager y DocumentDB

## 1) Crear el stack

Desde la raíz del repo:

```bash
aws cloudformation deploy ^
  --template-file infrastructure/cloudformation.yaml ^
  --stack-name btg-fondos-api ^
  --capabilities CAPABILITY_NAMED_IAM ^
  --parameter-overrides ^
    AppName=btg-fondos ^
    ImageTag=latest ^
    DesiredCount=1 ^
    DocDbInstanceClass=db.t3.medium
```

Si usas PowerShell, reemplaza `^` por backtick (`` ` ``) o ejecútalo en una sola línea.

## 2) Obtener URI del repositorio ECR

La plantilla deja el output `EcrRepositoryUri`. Puedes consultarlo así:

```bash
aws cloudformation describe-stacks ^
  --stack-name btg-fondos-api ^
  --query "Stacks[0].Outputs[?OutputKey=='EcrRepositoryUri'].OutputValue" ^
  --output text
```

## 3) Build y push de la imagen

```bash
aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <CUENTA>.dkr.ecr.<REGION>.amazonaws.com
docker build -t btg-fondos:latest .
docker tag btg-fondos:latest <ECR_REPOSITORY_URI>:latest
docker push <ECR_REPOSITORY_URI>:latest
```

## 4) Forzar nuevo despliegue en ECS

```bash
aws ecs update-service ^
  --cluster btg-fondos-cluster ^
  --service btg-fondos-api ^
  --force-new-deployment ^
  --region <REGION>
```

## 5) Validar

Toma el output `LoadBalancerDns` y prueba:

- `http://<dns>/health`
- `http://<dns>/docs`

## Actualizar una versión nueva

Cada cambio de código:

1. Build de imagen
2. Push con el mismo tag (`latest`) o uno nuevo
3. `aws ecs update-service --force-new-deployment`

Si usas tags distintos de `latest`, actualiza el parámetro `ImageTag` con `cloudformation deploy`.

## Notas importantes

- DocumentDB usa TLS; la API ya se configura con URI para TLS.
- La plantilla aplica `Snapshot` al cluster de DocumentDB para evitar pérdida de datos en reemplazos.
- El NAT Gateway genera costo fijo mensual + tráfico.
