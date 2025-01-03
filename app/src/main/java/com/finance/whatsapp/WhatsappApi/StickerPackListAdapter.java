
package com.finance.whatsapp.WhatsappApi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.finance.whatsapp.Constants.Constants;
import com.finance.whatsapp.Fragments.MyStickersFragment;
import com.finance.whatsapp.R;
import com.finance.whatsapp.utils.FileUtils;
import com.finance.whatsapp.utils.ImageUtils;
import com.finance.whatsapp.utils.StickerPacksManager;

import java.util.List;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {
    @NonNull
    private List<StickerPack> stickerPacks;
    @NonNull
    private final OnAddButtonClickedListener onAddButtonClickedListener;
    private int maxNumberOfStickersInARow;
    private MyStickersFragment parent;

    public StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks, @NonNull OnAddButtonClickedListener onAddButtonClickedListener, MyStickersFragment parent) {
        this.stickerPacks = stickerPacks;
        this.onAddButtonClickedListener = onAddButtonClickedListener;
        this.parent = parent;
    }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int i) {
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, final int index) {
        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.publisherView.getContext();
        viewHolder.publisherView.setText(pack.publisher);
        viewHolder.filesizeView.setText(FileUtils.getFolderSizeLabel(Constants.STICKERS_DIRECTORY_PATH + pack.identifier));

        viewHolder.titleView.setText(pack.name);
        viewHolder.container.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack);
            view.getContext().startActivity(intent);
        });
        viewHolder.imageRowView.removeAllViews();
        //if this sticker pack contains less stickers than the max, then take the smaller size.
        int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, pack.getStickers().size());
        for (int i = 0; i < actualNumberOfStickersToShow; i++) {
            final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_pack_list_item_image, viewHolder.imageRowView, false);
            rowImage.setImageURI(ImageUtils.getStickerImageAsset(pack.identifier, pack.getStickers().get(i).imageFileName));
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
            final int marginBetweenImages = (viewHolder.imageRowView.getMeasuredWidth() - maxNumberOfStickersInARow * viewHolder.imageRowView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)) / (maxNumberOfStickersInARow - 1) - lp.leftMargin - lp.rightMargin;
            if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + marginBetweenImages, lp.bottomMargin);
                rowImage.setLayoutParams(lp);
            }
            viewHolder.imageRowView.addView(rowImage);
        }
        setAddButtonAppearance(viewHolder.addButton, pack);

        viewHolder.container.setOnLongClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, viewHolder.addButton);
            popupMenu.inflate(R.menu.sticker_option_menu);
            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getItemId() == R.id.sticker_delete) {
                    new AlertDialog.Builder(context, R.style.DialogTheme)
                            .setTitle("Deleting")
                            .setMessage("Are you sure you want to delete this sticker pack?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                removeStickerPack(index);
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                parent.verifyStickersCount();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return false;
            });
            popupMenu.show();
            return false;
        });
    }

    private void removeStickerPack(int index) {
        stickerPacks.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, stickerPacks.size());
        StickerPacksManager.deleteStickerPack(index);
    }

    private void setAddButtonAppearance(ImageView addButton, StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);
            setBackground(addButton);
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add);
            addButton.setOnClickListener(v -> onAddButtonClickedListener.onAddButtonClicked(pack));
            TypedValue outValue = new TypedValue();
            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view) {
        view.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void setMaxNumberOfStickersInARow(int maxNumberOfStickersInARow) {
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks = stickerPackList;
        notifyDataSetChanged();
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }
}
