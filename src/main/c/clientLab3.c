#include <pthread.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdio.h>

#define BUF_SIZE 1024
#define NUM_THREADS     5

//struktura zawierajÄca dane, ktĂłre zostanÄ przekazane do wÄtku
struct thread_data_t
{
    int conn_sck_desc;
};

//wskaĹşnik na funkcjÄ opisujÄcÄ zachowanie wÄtku
void *ThreadBehavior(void *t_data)
{
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;
    
    int conn_sck = (*th_data).conn_sck_desc;
    
    char bufor[100];
    memset(bufor,0,100);
    int readC = 0;
    char *res;
    
    printf("%d:\nTryb wysylania\n:",conn_sck);
    
    //res = fgets(bufor, 10, stdin);
    bufor[0]='1';

    while(bufor[0]!='0'){
        if(bufor[0]=='0')break;
        fgets(bufor, 100, stdin);
        write(conn_sck,bufor,100);
    }
    pthread_exit(NULL);
}


//funkcja obsĹugujÄca poĹÄczenie z serwerem
void handleConnection(int connection_socket_descriptor) {
    //wynik funkcji tworzÄcej wÄtek
    int create_result = 0;
    printf("%d\n",connection_socket_descriptor);
    //uchwyt na wÄtek
    pthread_t thread1;
    
    //dane, ktĂłre zostanÄ przekazane do wÄtku
    struct thread_data_t *t_data;
    t_data = malloc(sizeof t_data);
    (*t_data).conn_sck_desc = connection_socket_descriptor;
    
    create_result = pthread_create(&thread1, NULL, ThreadBehavior, (void *)t_data);
    if (create_result){
       printf("BĹÄd przy prĂłbie utworzenia wÄtku, kod bĹÄdu: %d\n", create_result);
       exit(-1);
    }
    //free(t_data);
    
    char bufor[100];
    memset(bufor,0,100);
    int readC = 0;
    
    while((readC = read(connection_socket_descriptor, bufor, 100))>0){
        if(bufor[0]=='0')break;
        printf("Server: %s",bufor);
    }
}


int main (int argc, char *argv[])
{
   int connection_socket_descriptor;
   int connect_result;
   struct sockaddr_in server_address;
   struct hostent* server_host_entity;

   if (argc != 3)
   {
     fprintf(stderr, "SposĂłb uĹźycia: %s server_name port_number\n", argv[0]);
     exit(1);
   }

   server_host_entity = gethostbyname(argv[1]);
   if (! server_host_entity)
   {
      fprintf(stderr, "%s: Nie moĹźna uzyskaÄ adresu IP serwera.\n", argv[0]);
      exit(1);
   }

   connection_socket_descriptor = socket(PF_INET, SOCK_STREAM, 0);
   if (connection_socket_descriptor < 0)
   {
      fprintf(stderr, "%s: BĹÄd przy probie utworzenia gniazda.\n", argv[0]);
      exit(1);
   }

   memset(&server_address, 0, sizeof(struct sockaddr));
   server_address.sin_family = AF_INET;
   memcpy(&server_address.sin_addr.s_addr, server_host_entity->h_addr, server_host_entity->h_length);
   server_address.sin_port = htons(atoi(argv[2]));

   connect_result = connect(connection_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
   if (connect_result < 0)
   {
      fprintf(stderr, "%s: BĹÄd przy prĂłbie poĹÄczenia z serwerem (%s:%i).\n", argv[0], argv[1], atoi(argv[2]));
      exit(1);
   }

   handleConnection(connection_socket_descriptor);

   close(connection_socket_descriptor);
   return 0;

}
