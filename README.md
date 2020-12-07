# Simulação da criação de um “Sistema de Arquivos”  
## Trabalho de Sistemas Operacionais II.  
## Prof.: Marcio Piva.  
## Universidade Católica de Santos.  

### Integrantes  
#### Nome: Alexandre Saura.  
#### Nome: Fábio Thomaz.  
#### Nome: Maurício Freire.  
#### Nome: Ricardo Oliveira.  
#### Nome: Rodrigo Suarez.  
#### Nome: Vinnicius Stein.  

### Descrição  
> Construir um arquivo para simular um "sistema de arquivos" do estilo "jar", "zip", etc. Não há necessidade de compactação, somente concatenação de vários arquivos (em formato binário).  

### Funções Gerais  
1 - listar o conteúdo do sistema de arquivos (somente metadados);  
2 - inserir novos arquivos no final do sistema de arquivos (append);  
3 - remover arquivo de qualquer ponto, restaurando-o para o sistema de arquivo origem, dando opção de apontamento de pasta destino;  
4 - exibir o conteúdo de um arquivo (somente tipo texto);  
5 - desfragmentação do sistema de arquivos;  
6 - inserção em melhor "partição", seguindo o critério best-fit (alocação contígua);  
7 - inserção aproveitando todas as partições livres, com critério de alocação encadeada;  
8 - inserção aproveitando todas as partições livres, com critério de alocação indexada (um nível);  
9 - criação de diretórios para melhor organização (um único nível);  
10 - criação de diretórios para melhor organização (em arvore).  
