package com.facens.firebase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facens.firebase.adapter.ProdutoAdapter;
import com.facens.firebase.model.Produto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db;
    private EditText edtNome, edtEstoque;
    private RecyclerView recyclerProdutos;
    private List<Produto> listaProdutos = new ArrayList<>();
    private ProdutoAdapter produtoAdapter;
    private Produto produtoEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.edtNome);
        edtEstoque = findViewById(R.id.edtEstoque);;
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        produtoAdapter = new ProdutoAdapter(listaProdutos);
        recyclerProdutos.setAdapter(produtoAdapter);

        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarProduto());
    }

    private void salvarProduto() {
        String nome = edtNome.getText().toString();
        int estoque = Integer.parseInt(edtEstoque.getText().toString());

        if (produtoEditando == null) {
            // Criar novo
            Produto produto = new Produto(null, nome, estoque);
            db.collection("produtos")
                    .add(produto)
                    .addOnSuccessListener(doc -> {
                        produto.setId(doc.getId());
                        Toast.makeText(this, "Produto salvo!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarProdutos();
                    });
        } else {
            // Atualizar existente
            produtoEditando.setNome(nome);
            produtoEditando.setEstoque(estoque);

            db.collection("produtos").document(produtoEditando.getId())
                    .set(produtoEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Produto atualizado!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarProdutos();
                    });
        }
    }

    private void carregarProdutos() {
        db.collection("produtos")
                .get()
                .addOnSuccessListener(query -> {
                    listaProdutos.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Produto p = doc.toObject(Produto.class);
                        p.setId(doc.getId());
                        listaProdutos.add(p);
                    }
                    produtoAdapter.notifyDataSetChanged();
                });

        produtoAdapter.setOnItemClickListener(produto -> {
            edtNome.setText(produto.getNome());
            edtEstoque.setText(String.valueOf(produto.getEstoque()));
            produtoEditando = produto;
            ((Button) findViewById(R.id.btnSalvar)).setText("Atualizar Produto");
        });

    }

    private void limparCampos() {
        edtNome.setText("");
        edtEstoque.setText("");
        produtoEditando = null;
        ((Button) findViewById(R.id.btnSalvar)).setText("Salvar Produto");
    }

    public void cadastrarUsuario (View v) {
        EditText edtEmail = findViewById(R.id.editTextTextEmailAddress);
        EditText edtPass = findViewById(R.id.editTextTextPassword);

        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(this, "Usuário criado com sucesso", Toast.LENGTH_SHORT).show();
                        Log.d("Firebase", "Usuário criado");
                    } else {
                        Toast.makeText(this, "falha ao criar usuário", Toast.LENGTH_SHORT).show();
                        Log.d("Firebase", "Erro ao criar Usuário", task.getException());
                    }
                });
    }

    public void logarUsuario (View v) {
        EditText edtEmail = findViewById(R.id.editTextTextEmailAddress);
        EditText edtPass = findViewById(R.id.editTextTextPassword);

        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPass.getText().toString())
                .addOnCompleteListener(task ->  {
                   if(task.isSuccessful()) {
                       Toast.makeText(this, "Login autorizado", Toast.LENGTH_SHORT).show();
                       Log.d("Firebase", "Usuário logou");
                   } else {
                       Toast.makeText(this, "falha ao logar", Toast.LENGTH_SHORT).show();
                       Log.d("Firebase", "Erro ao logar", task.getException());
                   }
                });
    }
}